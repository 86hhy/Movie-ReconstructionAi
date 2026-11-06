const allowedOrigin = process.env.CORS_ALLOWED_ORIGIN || 'https://86hhy.github.io';

function cors(req, res) {
  const origin = req.headers.origin;
  if (origin && origin !== allowedOrigin) {
    res.status(403).json({ error: 'This origin is not allowed to use the video API.' });
    return false;
  }
  if (origin) res.setHeader('Access-Control-Allow-Origin', origin);
  res.setHeader('Vary', 'Origin');
  res.setHeader('Access-Control-Allow-Methods', 'GET, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');
  res.setHeader('Access-Control-Max-Age', '86400');
  return true;
}

export default async function handler(req, res) {
  if (!cors(req, res)) return;
  if (req.method === 'OPTIONS') return res.status(204).end();
  if (req.method !== 'GET') return res.status(405).json({ error: 'Use GET to check a prediction.' });
  const token = process.env.REPLICATE_API_TOKEN;
  if (!token) return res.status(503).json({ error: 'Video generation is not configured on the server.' });
  const id = typeof req.query?.id === 'string' ? req.query.id : '';
  if (!/^[a-zA-Z0-9_-]{8,128}$/.test(id)) return res.status(400).json({ error: 'A valid prediction ID is required.' });

  try {
    const upstream = await fetch(`https://api.replicate.com/v1/predictions/${encodeURIComponent(id)}`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    const result = await upstream.json().catch(() => ({}));
    if (!upstream.ok) {
      const detail = result.detail || result.title || result.error || `Replicate returned HTTP ${upstream.status}.`;
      return res.status(upstream.status >= 400 && upstream.status < 600 ? upstream.status : 502).json({ error: String(detail).slice(0, 1000) });
    }
    res.setHeader('Cache-Control', 'no-store');
    return res.status(200).json(result);
  } catch (error) {
    return res.status(502).json({ error: `Could not connect to Replicate: ${error.message}` });
  }
}
