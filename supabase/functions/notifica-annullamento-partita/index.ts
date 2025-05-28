// Follow this setup guide to integrate the Deno language server with your editor:
// https://deno.land/manual/getting_started/setup_your_environment
// This enables autocomplete, go to definition, etc.

// Setup type definitions for built-in Supabase Runtime APIs
import { serve } from "https://deno.land/std@0.177.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

function formattaData(dataISO: string): string {
  const data = new Date(dataISO);
  return `${data.toLocaleDateString("it-IT")} alle ${data.toLocaleTimeString("it-IT", {
    hour: "2-digit",
    minute: "2-digit",
  })}`;
}

serve(async (_req) => {
  try {
    const supabase = createClient(
      Deno.env.get("SUPABASE_URL")!,
      Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!
    );

    const now = new Date().toISOString();

    // Prendi partite scadute + data + luogo + campo (join manuale)
    const { data: partiteScadute, error: errorPartite } = await supabase
      .from("partite")
      .select("idPartita, maxGiocatori, dataOraInizio, luogo, campi_sportivi(nome, citta)")
      .lte("dataOraScadenzaIscrizione", now);

    if (errorPartite) throw errorPartite;

    const notifiche: any[] = [];
    const idPartiteDaEliminare: number[] = [];

    for (const partita of partiteScadute || []) {
      const { idPartita, maxGiocatori, dataOraInizio, campi_sportivi } = partita;

      const luogoDescrizione = campi_sportivi
        ? `${campi_sportivi.nome} (${campi_sportivi.citta})`
        : "luogo sconosciuto";

      const dataFormattata = formattaData(dataOraInizio);

      // Conta i giocatori iscritti
      const { data: giocatori, error: errorGiocatori } = await supabase
        .from("giocatori_squadra")
        .select("utente")
        .eq("partita", idPartita);

      if (errorGiocatori) throw errorGiocatori;

      if ((giocatori?.length || 0) < maxGiocatori) {
        idPartiteDaEliminare.push(idPartita);

        for (const g of giocatori || []) {
			const email = g.utente;
			
			// Recupera il token FCM dell'utente
			const { data: utente, error: errorUtente } = await supabase
				.from("utenti")
				.select("fcm_token")
				.eq("email", email)
				.single();
			
			if (errorUtente) {
				console.warn(`Errore recupero fcm_token per ${email}:`, errorUtente.message);
			}
			
			const fcmToken = utente?.fcm_token;
			
			// Costruzione della notifica
			const notifica = {
				titolo: "Partita annullata",
				testo: `La partita del ${dataFormattata} presso ${luogoDescrizione} è stata annullata per mancanza di iscrizioni sufficienti.`,
				destinatario: email,
				partita: null,
				tipologia: "annulla",
				titolo_en: "Match cancelled",
				testo_en: `The match on ${dataFormattata} at ${luogoDescrizione} was cancelled due to insufficient registrations.`,
			};
			
			notifiche.push(notifica); // viene comunque inserita nel DB
			
			// Invia push solo se esiste fcmToken
			if (fcmToken) {
				try {
				const pushResponse = await fetch("https://fcm-proxy.onrender.com/api/send-notification", {
					method: "POST",
					headers: { "Content-Type": "application/json" },
					body: JSON.stringify({
					fcmToken,
					notificaJson: {
						titolo: notifica.titolo,
						testo: notifica.testo,
					},
					}),
				});
			
				if (pushResponse.ok) {
					console.log(`Push inviata a ${email}`);
				} else {
					const err = await pushResponse.text();
					console.warn(`Push fallita per ${email}:`, err);
				}
				} catch (err) {
				console.error(`Errore invio push a ${email}:`, err);
				}
			} else {
				console.log(`Nessun fcmToken per ${email}, aggiunta solo al DB`);
			}
		}
    }

    // Invia notifiche
    if (notifiche.length > 0) {
      const { error: errorInsert } = await supabase
        .from("notifiche")
        .insert(notifiche);
      if (errorInsert) throw errorInsert;
    }

    // Elimina le partite
    if (idPartiteDaEliminare.length > 0) {
      const { error: errorDelete } = await supabase
        .from("partite")
        .delete()
        .in("idPartita", idPartiteDaEliminare);
      if (errorDelete) throw errorDelete;
    }

    return new Response(
      JSON.stringify({
        success: true,
        messaggio: "Notifiche inviate e partite eliminate",
        partiteEliminate: idPartiteDaEliminare.length,
        notificheInviate: notifiche.length,
      }),
      { headers: { "Content-Type": "application/json" } }
    );
  } catch (e) {
    console.error("Errore:", e);
    return new Response(
      JSON.stringify({ success: false, error: JSON.stringify(e) }),
      { status: 500, headers: { "Content-Type": "application/json" } }
    );
  }
});




/* To invoke locally:

  1. Run `supabase start` (see: https://supabase.com/docs/reference/cli/supabase-start)
  2. Make an HTTP request:

  curl -i --location --request POST 'http://127.0.0.1:54321/functions/v1/notifica-annullamento-partita' \
    --header 'Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZS1kZW1vIiwicm9sZSI6ImFub24iLCJleHAiOjE5ODM4MTI5OTZ9.CRXP1A7WOeoJeXxjNni43kdQwgnWNReilDMblYTn_I0' \
    --header 'Content-Type: application/json' \
    --data '{"name":"Functions"}'

*/
