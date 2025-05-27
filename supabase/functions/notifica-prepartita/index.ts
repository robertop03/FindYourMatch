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

    const now = new Date();
    const oneHourLater = new Date(now.getTime() + 60 * 60 * 1000);

    const { data: partite, error: errorPartite } = await supabase
      .from("partite")
      .select("idPartita, dataOraInizio")
      .gte("dataOraInizio", now.toISOString())
      .lte("dataOraInizio", oneHourLater.toISOString());

    if (errorPartite) throw errorPartite;

    const notifiche = [];

    for (const partita of partite || []) {
      const idPartita = partita.idPartita;

      const { data: giocatori, error: errorGiocatori } = await supabase
        .from("giocatori_squadra")
        .select("utente")
        .eq("partita", idPartita);

      if (errorGiocatori) throw errorGiocatori;

      for (const g of giocatori || []) {
        const email = g.utente;

        const { data: esiste, error: errorCheck } = await supabase
          .from("notifiche")
          .select("idNotifica")
          .eq("partita", idPartita)
          .eq("destinatario", email)
          .eq("tipologia", "partita")
          .maybeSingle();

        if (errorCheck) throw errorCheck;
        if (esiste) continue;

        notifiche.push({
          titolo: "Sta per iniziare una partita!",
          testo: "Preparati! Il tuo calcetto inizia tra un'ora.",
          destinatario: email,
          partita: idPartita,
          tipologia: "partita",
          titolo_en: "Your match is coming up!",
          testo_en: "Get ready! Your match starts in one hour.",
        });
      }
    }

    if (notifiche.length > 0) {
      const { error: errorInsert } = await supabase
        .from("notifiche")
        .insert(notifiche);
      if (errorInsert) throw errorInsert;
    }

    return new Response(
      JSON.stringify({
        success: true,
        messaggio: "Notifiche inviate con successo",
        count: notifiche.length,
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

  curl -i --location --request POST 'http://127.0.0.1:54321/functions/v1/notifica-prepartita' \
    --header 'Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZS1kZW1vIiwicm9sZSI6ImFub24iLCJleHAiOjE5ODM4MTI5OTZ9.CRXP1A7WOeoJeXxjNni43kdQwgnWNReilDMblYTn_I0' \
    --header 'Content-Type: application/json' \
    --data '{"name":"Functions"}'

*/
