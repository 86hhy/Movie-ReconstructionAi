const allowedOrigin = process.env.CORS_ALLOWED_ORIGIN || 'https://86hhy.github.io';

function cors(req, res) {
  const origin = req.headers.origin;
  if (origin && origin !== allowedOrigin) {
    res.status(403).json({ error: 'This origin is not allowed to use the video API.' });
    return false;
  }
  if (origin) res.setHeader('Access-Control-Allow-Origin', origin);
  res.setHeader('Vary', 'Origin');
  res.setHeader('Access-Control-Allow-Methods', 'POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');
  res.setHeader('Access-Control-Max-Age', '86400');
  return true;
}

export default async function handler(req, res) {
  if (!cors(req, res)) return;
  if (req.method === 'OPTIONS') return res.status(204).end();
  if (req.method !== 'POST') return res.status(405).json({ error: 'Use POST to start a video generation.' });
  const token = process.env.REPLICATE_API_TOKEN;
  if (!token) return res.status(503).json({ error: 'Video generation is not configured: the server is missing its Replicate token secret.' });

  const prompt = typeof req.body?.prompt === 'string' ? req.body.prompt.trim() : '';
  if (!prompt || prompt.length > 1800) return res.status(400).json({ error: 'Provide a prompt from 1 to 1800 characters.' });
  const model = process.env.REPLICATE_MODEL || 'google/veo-3';
  if (!/^[a-z0-9-]+\/[a-z0-9._-]+$/i.test(model)) return res.status(500).json({ error: 'REPLICATE_MODEL must be written as owner/model.' });

  try {
    const upstream = await fetch(`https://api.replicate.com/v1/models/${model}/predictions`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json', Prefer: 'wait=1' },
      body: JSON.stringify({ input: { prompt } })
    });
    const result = await upstream.json().catch(() => ({}));
    if (!upstream.ok) {
      const detail = result.detail || result.title || result.error || `Replicate returned HTTP ${upstream.status}.`;
      return res.status(upstream.status >= 400 && upstream.status < 600 ? upstream.status : 502).json({ error: String(detail).slice(0, 1000) });
    }
    res.setHeader('Cache-Control', 'no-store');
    return res.status(202).json(result);
  } catch (error) {
    return res.status(502).json({ error: `Could not connect to Replicate: ${error.message}` });
  }
}
