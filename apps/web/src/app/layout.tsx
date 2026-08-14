import type { Metadata, Viewport } from "next";
import type { ReactNode } from "react";
import { getCurrentMember } from "@/lib/auth";
import { SiteHeader } from "@/components/site-header";
import "./globals.css";

export const metadata: Metadata = {
  title: {
    default: "Cobblemon Kinetics",
    template: "%s · Cobblemon Kinetics",
  },
  description:
    "A private-first design studio and published wiki for Pokémon-powered Create automation.",
  applicationName: "Cobblemon Kinetics Workshop",
};

export const viewport: Viewport = {
  colorScheme: "dark light",
  themeColor: "#191a19",
};

export default async function RootLayout({ children }: Readonly<{ children: ReactNode }>) {
  const member = await getCurrentMember();
  return (
    <html lang="en" data-scroll-behavior="smooth">
      <body>
        <a className="skip-link" href="#main-content">
          Skip to content
        </a>
        <SiteHeader member={member} />
        <div id="main-content">{children}</div>
        <footer className="site-footer">
          <p>
            <strong>Cobblemon Kinetics</strong> · An independent open-source compatibility project.
          </p>
          <p>No third-party game art is distributed by this website.</p>
        </footer>
      </body>
    </html>
  );
}
