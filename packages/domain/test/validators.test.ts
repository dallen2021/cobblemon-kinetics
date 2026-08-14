import { describe, expect, it } from "vitest";

import {
  assertPublicSafe,
  isResourceLocation,
  validateWorkProfile,
  type WorkProfile,
} from "../src/index.js";

const hydroProfile: WorkProfile = {
  format_version: 1,
  id: "cobblemon_kinetics:hydro_operator",
  title: "Hydro Operator",
  priority: 0,
  status: "experimental",
  selector: {
    kind: "type",
    types: ["cobblemon:water"],
    national_dex: { min: 1, max: 151 },
  },
  constraints: {
    requires_owner: true,
    must_be_alive: true,
    must_not_be_fainted: true,
    must_not_be_battling: true,
    must_be_idle: true,
  },
  workstation: {
    adapter_id: "cobblemon_kinetics:hydro_coupler",
    registry_ids: ["cobblemon_kinetics:hydro_coupler"],
    required_attachment_tag: "create:water_wheels",
    radius: 6,
  },
  contribution: {
    mode: "fixed",
    rpm: 8,
    capacity_per_rpm: 64,
    efficiency_multiplier: 1,
  },
  public_rationale: "Water-type workers drive the attached wheel without placing water blocks.",
};

describe("domain validation", () => {
  it("accepts the versioned Hydro profile", () => {
    expect(validateWorkProfile(hydroProfile)).toMatchObject({ ok: true, errors: [] });
  });

  it("rejects unknown format versions and invalid resource locations", () => {
    expect(validateWorkProfile({ ...hydroProfile, format_version: 2 }).ok).toBe(false);
    expect(isResourceLocation("Not Valid")).toBe(false);
  });

  it("blocks private fields from public projections", () => {
    expect(() => assertPublicSafe({ public_id: "test:record", private_note: "no" })).toThrow(
      /private or quarantined fields/,
    );
  });
});
