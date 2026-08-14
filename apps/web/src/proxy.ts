import { createServerClient } from "@supabase/ssr";
import { type NextRequest, NextResponse } from "next/server";
import { requireSafeSupabaseUrl } from "@/lib/supabase/url-policy";
import type { Database } from "@/types/database.generated";

const anonymousPaths = new Set([
  "/auth/sign-in",
  "/auth/callback",
  "/auth/denied",
  "/api/health",
  "/maintenance",
]);

const disabledPaths = new Set(["/api/health", "/maintenance"]);

function isLoopbackHostname(hostname: string): boolean {
  return hostname === "localhost" || hostname === "127.0.0.1" || hostname === "[::1]";
}

function supabaseEnvironment(): { url: string; publishableKey: string } | null {
  const url = process.env.NEXT_PUBLIC_SUPABASE_URL;
  const publishableKey = process.env.NEXT_PUBLIC_SUPABASE_PUBLISHABLE_KEY;
  if (!url || !publishableKey) return null;
  try {
    return { url: requireSafeSupabaseUrl(url).toString(), publishableKey };
  } catch {
    return null;
  }
}

function safeFixtureMode(request: NextRequest): boolean {
  const production =
    process.env.NODE_ENV === "production" ||
    process.env.VERCEL === "1" ||
    Boolean(process.env.VERCEL_ENV);
  if (process.env.STUDIO_FIXTURE_MODE !== "true" || production) return false;
  try {
    const appBaseUrl = new URL(process.env.APP_BASE_URL ?? "");
    const loopback = isLoopbackHostname(appBaseUrl.hostname);
    return (
      loopback &&
      isLoopbackHostname(request.nextUrl.hostname) &&
      (appBaseUrl.protocol === "http:" || appBaseUrl.protocol === "https:") &&
      !appBaseUrl.username &&
      !appBaseUrl.password &&
      !appBaseUrl.search &&
      !appBaseUrl.hash &&
      (appBaseUrl.pathname === "/" || appBaseUrl.pathname === "")
    );
  } catch {
    return false;
  }
}

function isProtected(pathname: string): boolean {
  if (anonymousPaths.has(pathname)) return false;
  const mode = process.env.SITE_ACCESS_MODE ?? "private";
  return pathname.startsWith("/studio") || mode === "private";
}

export async function proxy(request: NextRequest) {
  const pathname = request.nextUrl.pathname;
  const mode = process.env.SITE_ACCESS_MODE ?? "private";
  if (mode === "disabled" && !disabledPaths.has(pathname)) {
    return NextResponse.redirect(new URL("/maintenance", request.url));
  }

  if (process.env.STUDIO_FIXTURE_MODE === "true" && !safeFixtureMode(request)) {
    return NextResponse.json({ error: "Local fixture access rejected." }, { status: 403 });
  }

  const environment = supabaseEnvironment();
  if (!environment) {
    if (safeFixtureMode(request)) {
      return NextResponse.next();
    }
    if (isProtected(pathname)) {
      const url = new URL("/auth/sign-in", request.url);
      url.searchParams.set("next", `${pathname}${request.nextUrl.search}`);
      return NextResponse.redirect(url);
    }
    return NextResponse.next();
  }

  let response = NextResponse.next({ request });
  const supabase = createServerClient<Database>(environment.url, environment.publishableKey, {
    cookies: {
      getAll: () => request.cookies.getAll(),
      setAll(cookiesToSet) {
        for (const { name, value } of cookiesToSet) {
          request.cookies.set(name, value);
        }
        response = NextResponse.next({ request });
        for (const { name, value, options } of cookiesToSet) {
          response.cookies.set(name, value, options);
        }
      },
    },
  });

  const { data } = await supabase.auth.getClaims();
  if (isProtected(pathname) && !data?.claims?.sub) {
    const url = new URL("/auth/sign-in", request.url);
    url.searchParams.set("next", `${pathname}${request.nextUrl.search}`);
    return NextResponse.redirect(url);
  }
  if (isProtected(pathname) || pathname.startsWith("/auth")) {
    response.headers.set("Cache-Control", "private, no-store");
  }
  return response;
}

export const config = {
  matcher: [
    "/((?!_next/static|_next/image|favicon.ico|robots.txt|sitemap.xml|.*\\.(?:css|js|map|png|jpg|jpeg|gif|webp|ico)$).*)",
  ],
};
