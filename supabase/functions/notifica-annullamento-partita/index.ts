// Follow this setup guide to integrate the Deno language server with your editor:
// https://deno.land/manual/getting_started/setup_your_environment
// This enables autocomplete, go to definition, etc.

// Setup type definitions for built-in Supabase Runtime APIs
import { serve } from "https://deno.land/std@0.177.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

serve(async (_req) => {
  try {
    const supabase = createClient(
      Deno.env.get("SUPABASE_URL")!,
      Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!
    );

    const now = new Date().toISOString();

    // Prendi solo partite con iscrizioni scadute
    const { data: partiteScadute, error: errorPartite } = await supabase
      .from("partite")
      .select("idPartita, maxGiocatori")
      .lte("dataOraFineIscrizioni", now);

    if (errorPartite) throw errorPartite;

    const notifiche: any[] = [];
    const idPartiteDaEliminare: number[] = [];

    for (const partita of partiteScadute || []) {
      const idPartita = partita.idPartita;
      const maxGiocatori = partita.maxGiocatori;

      // Conta i giocatori iscritti
      const { data: giocatori, error: errorGiocatori } = await supabase
        .from("giocatori_squadra")
        .select("utente")
        .eq("partita", idPartita);

      if (errorGiocatori) throw errorGiocatori;

      // Solo se giocatori < maxGiocatori si procede
      if ((giocatori?.length || 0) < maxGiocatori) {
        idPartiteDaEliminare.push(idPartita);

        for (const g of giocatori || []) {
          const email = g.utente;

          notifiche.push({
            titolo: "Partita annullata",
            testo: "La partita è stata annullata per mancanza di iscrizioni sufficienti.",
            destinatario: email,
            partita: idPartita,
            tipologia: "annulla",
            titolo_en: "Match cancelled",
            testo_en: "The match was cancelled due to insufficient registrations.",
          });
        }
      }
    }

    // Invia notifiche PRIMA dell’eliminazione
    if (notifiche.length > 0) {
      const { error: errorInsert } = await supabase
        .from("notifiche")
        .insert(notifiche);
      if (errorInsert) throw errorInsert;
    }

    // Elimina partite con giocatori insufficienti
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
        messaggio: "Notifiche inviate e partite con giocatori insufficienti eliminate",
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
