"use strict";

const catalog = require("./data/reading-catalog.json");
const { ENGLISH_BASE_PROMPT, SYSTEM_MESSAGES } = require("./reading-prompts");
const formats = new Map(catalog.formats.map(format => [format.id, format]));
const cards = new Map(catalog.cards.map(card => [card.id, card]));

// Keep behavior unchanged; migration and provider settings have one explicit home.
const SETTINGS = Object.freeze({
  url: "https://api.groq.com/openai/v1/chat/completions",
  model: "openai/gpt-oss-120b", maxTokens: 2048, topP: 0.9,
  maxPromptLength: 14000, requestTimeoutMs: 45000,
  allowLegacyRequests: true, // Disable only after old Android versions have been retired.
});

function fail(code) { throw Object.assign(new Error(code), { code }); }
function onlyKeys(data, keys) { return Object.keys(data).every(key => keys.includes(key)); }

function prepareReading(data, { allowLegacy = SETTINGS.allowLegacyRequests } = {}) {
  if (!data || typeof data !== "object" || Array.isArray(data)) fail("invalid-request");
  if (data.schemaVersion === undefined && allowLegacy) {
    if (!onlyKeys(data, ["prompt", "isTurkish"]) || typeof data.isTurkish !== "boolean" ||
        typeof data.prompt !== "string" || !data.prompt.trim() || data.prompt.length > SETTINGS.maxPromptLength) fail("invalid-request");
    // Compatibility only: the old API accepts free-form prompts. Never fall back here from v2.
    return { prompt: data.prompt, language: data.isTurkish ? "tr" : "en", legacy: true };
  }
  if (data.schemaVersion !== 2 || !onlyKeys(data, ["schemaVersion", "spreadId", "language", "cardIds"]) ||
      !["tr", "en"].includes(data.language)) fail("invalid-request");
  const format = formats.get(data.spreadId);
  if (!format || !Array.isArray(data.cardIds) || data.cardIds.length !== format.cardCount ||
      new Set(data.cardIds).size !== data.cardIds.length ||
      data.cardIds.some(id => typeof id !== "string" || !cards.has(id))) fail("invalid-request");
  const selected = data.cardIds.map(id => cards.get(id)); // Array order is slot order.
  const prompt = buildPrompt(format, selected, data.language);
  if (prompt.length > SETTINGS.maxPromptLength) fail("invalid-request");
  return { prompt, language: data.language, legacy: false };
}

function buildPrompt(format, selected, language) {
  if (language === "tr") {
    let prompt = format.promptTr;
    selected.forEach((card, index) => {
      prompt = prompt.replaceAll(`[KART_${index + 1}_ADI]`, card.nameTr || card.name);
      if (index === 0) prompt = prompt.replaceAll("[KART_ADI]", card.nameTr || card.name);
    });
    const details = selected.map((card, i) => `${format.positionsTr[i]}: ${card.nameTr || card.name}\nAnlam: ${card.uprightTr}\nAnahtar Kelimeler: ${card.keywords.join(", ")}`).join("\n\n");
    return `${prompt}\n\n--- Kart Detayları ---\n${details}\n\n[ÖNEMLİ: Yanıtını SADECE Türkçe yaz. Hiçbir İngilizce kelime kullanma.]`;
  }
  const positions = selected.map((card, i) => `${format.positionsEn[i]}: ${card.name}`).join("\n");
  // Preserve the existing language-specific references; changing their meaning is a separate task.
  const details = selected.map((card, i) => `${format.positionsEn[i]}: ${card.name}\nUpright meaning: ${card.uprightEn}\nReversed meaning: ${card.reversedEn}`).join("\n\n");
  return `${ENGLISH_BASE_PROMPT}\n\nSpread Name: ${format.nameEn}\n\nCards and Positions:\n${positions}\n\n--- Card Reference ---\n${details}\n\n[IMPORTANT: Write the entire reading ONLY in English. Translate all headings into English. Do not copy Turkish phrasing.]`;
}

function completeReading(payload) {
  const choice = payload?.choices?.[0];
  if (choice?.finish_reason !== "stop" || typeof choice?.message?.content !== "string" ||
      !choice.message.content.trim() || choice.message.refusal || choice.message.tool_calls?.length) fail("incomplete-response");
  return choice.message.content.trim();
}

async function requestReading(prepared, apiKey, fetchImpl = fetch) {
  const response = await fetchImpl(SETTINGS.url, {
    method: "POST",
    headers: { Authorization: `Bearer ${apiKey}`, "Content-Type": "application/json" },
    signal: AbortSignal.timeout(SETTINGS.requestTimeoutMs),
    body: JSON.stringify({
      model: SETTINGS.model,
      messages: [{ role: "system", content: SYSTEM_MESSAGES[prepared.language] }, { role: "user", content: prepared.prompt }],
      temperature: prepared.language === "tr" ? 0.65 : 0.72,
      max_tokens: SETTINGS.maxTokens, top_p: SETTINGS.topP, stream: false,
    }),
  });
  if (!response.ok) fail(response.status === 429 ? "provider-busy" : "provider-failed");
  return completeReading(await response.json());
}

module.exports = { SETTINGS, prepareReading, completeReading, requestReading };
