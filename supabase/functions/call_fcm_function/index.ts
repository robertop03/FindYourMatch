// Follow this setup guide to integrate the Deno language server with your editor:
// https://deno.land/manual/getting_started/setup_your_environment
// This enables autocomplete, go to definition, etc.

// Setup type definitions for built-in Supabase Runtime APIs
import { serve } from "https://deno.land/std@0.177.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";
import { GoogleAuth } from "https://esm.sh/google-auth-library@8.7.0";

// import serviceAccount from "./service_account_key.json" assert { type: "json" };

serve(async (req) => {
  try {
    const payload = await req.json();
    const newNotifica = payload.record;

    const supabase = createClient(
      Deno.env.get("SUPABASE_URL")!,
      Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!
    );

    const { data, error } = await supabase
      .from("utenti")
      .select("fcm_token")
      .eq("email", newNotifica.destinatario)
      .single();

    if (error || !data?.fcm_token) {
      return new Response("FCM token mancante o errore Supabase", { status: 400 });
    }

    // Autenticazione tramite service_account_key.json
	const rawKey = Deno.env.get("GOOGLE_CREDENTIALS_JSON");

	if (!rawKey) {
		return new Response("Chiave FCM mancante", { status: 500 });
	}

	const credentials = JSON.parse(rawKey);
	
	const auth = new GoogleAuth({
		credentials,
		scopes: ["https://www.googleapis.com/auth/firebase.messaging"],
	});


    const client = await auth.getClient();
    const accessToken = await client.getAccessToken();

    // Costruzione payload FCM (API HTTP v1)
    const fcmPayload = {
      message: {
        token: data.fcm_token,
        notification: {
          title: newNotifica.titolo,
          body: newNotifica.testo,
        },
      },
    };

    const response = await fetch(
      "https://fcm.googleapis.com/v1/projects/findyourmatch-5bc41/messages:send",
      {
        method: "POST",
        headers: {
          Authorization: `Bearer ${accessToken.token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify(fcmPayload),
      }
    );

    if (!response.ok) {
      const errorText = await response.text();
      return new Response(`Errore invio FCM: ${errorText}`, { status: 500 });
    }

    return new Response("Notifica push inviata con successo!", { status: 200 });

  } catch (err) {
    return new Response(`Errore interno: ${err}`, { status: 500 });
  }
});



/* To invoke locally:

  1. Run `supabase start` (see: https://supabase.com/docs/reference/cli/supabase-start)
  2. Make an HTTP request:

  curl -i --location --request POST 'http://127.0.0.1:54321/functions/v1/call_fcm_function' \
    --header 'Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZS1kZW1vIiwicm9sZSI6ImFub24iLCJleHAiOjE5ODM4MTI5OTZ9.CRXP1A7WOeoJeXxjNni43kdQwgnWNReilDMblYTn_I0' \
    --header 'Content-Type: application/json' \
    --data '{"name":"Functions"}'

*/
