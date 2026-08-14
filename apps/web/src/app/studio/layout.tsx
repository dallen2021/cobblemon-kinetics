import type { ReactNode } from "react";
import { StudioShell } from "@/components/studio-shell";
import { enforcePageAccess } from "@/lib/auth";

export default async function StudioLayout({ children }: { children: ReactNode }) {
  const member = await enforcePageAccess("studio", "/studio");
  if (!member) return null;
  return <StudioShell member={member}>{children}</StudioShell>;
}
