import Link from "next/link";
import type { ReactNode } from "react";
import type { AppMember } from "@/lib/auth";
import { StatusLamp } from "./ui";

const sections = [
  ["Overview", "/studio"],
  ["Squirtle", "/studio/pokemon/squirtle"],
  ["Compatibility", "/studio/compatibility"],
  ["Workboard", "/studio/workboard"],
  ["Imports", "/studio/imports"],
  ["Publications", "/studio/publications"],
  ["Assets", "/studio/assets"],
  ["History", "/studio/history"],
  ["Access", "/studio/settings/access"],
] as const;

export function StudioShell({ member, children }: { member: AppMember; children: ReactNode }) {
  return (
    <div className="studio-shell">
      <aside className="studio-sidebar">
        <div>
          <p className="eyebrow">Development studio</p>
          <h2>Gen 1 workshop</h2>
        </div>
        <nav aria-label="Studio navigation">
          {sections.map(([label, href]) => (
            <Link href={href} key={href}>
              {label}
            </Link>
          ))}
        </nav>
        <div className="studio-identity">
          <StatusLamp
            tone={member.fixture ? "amber" : "green"}
            label={member.fixture ? "Safe fixture" : "Authenticated"}
          />
          <p>
            <strong>{member.displayName}</strong>
            <br />
            <span>{member.role}</span>
          </p>
        </div>
      </aside>
      <div className="studio-main">{children}</div>
    </div>
  );
}
