/*
 * Lettore minimo di Server-Sent Events su una risposta fetch().
 *
 * Perché non EventSource?
 *  - EventSource fa solo GET: la micro chat invia il messaggio con POST.
 *  - Lo standard SSE toglie UNO spazio dopo "data:". Spring scrive "data:" senza spazio, quindi un pezzo
 *    come " volta" (i token dei modelli iniziano spesso con uno spazio) arriverebbe come "volta"
 *    e le parole si attaccherebbero. Qui il testo dopo "data:" si tiene così com'è.
 *
 * Uso: await leggiEventiSse(risposta, (evento, dati) => { ... });
 */
async function leggiEventiSse(risposta, suEvento) {
  const lettore = risposta.body.getReader();
  const decoder = new TextDecoder();
  let buffer = '';

  const gestisciBlocco = (blocco) => {
    let evento = 'message';
    const dati = [];
    for (const riga of blocco.split('\n')) {
      if (riga.startsWith('event:')) evento = riga.slice(6).trim();
      else if (riga.startsWith('data:')) dati.push(riga.slice(5));
    }
    if (dati.length > 0) suEvento(evento, dati.join('\n'));
  };

  for (;;) {
    const { value, done } = await lettore.read();
    if (done) break;
    buffer += decoder.decode(value, { stream: true }).replace(/\r\n/g, '\n');
    let fine;
    while ((fine = buffer.indexOf('\n\n')) >= 0) {   // una riga vuota chiude ogni evento
      gestisciBlocco(buffer.slice(0, fine));
      buffer = buffer.slice(fine + 2);
    }
  }
  if (buffer.trim()) gestisciBlocco(buffer);
}
