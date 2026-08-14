import Link from "next/link";
import { HydroLine } from "@/components/hydro-line";
import { MaterialPanel, RegistryId, StatusLamp } from "@/components/ui";
import { enforcePageAccess } from "@/lib/auth";

export default async function HomePage() {
  await enforcePageAccess("home", "/");
  return (
    <main>
      <section className="hero">
        <div className="hero-copy">
          <p className="eyebrow">A worker-first factory experiment</p>
          <h1>Make the creature part of the machine.</h1>
          <p className="hero-lede">
            Cobblemon Kinetics explores Create automation powered by deliberate Pokémon
            jobs—designed, balanced, reviewed, and published as data.
          </p>
          <div className="hero-actions">
            <Link className="button button-primary" href="/wiki/pokemon/squirtle">
              Inspect the Hydro prototype
            </Link>
            <Link className="button button-secondary" href="/studio">
              Open the studio
            </Link>
          </div>
          <div className="hero-status">
            <StatusLamp tone="teal" label="Generation 1 scope" />
            <StatusLamp tone="amber" label="Private prototype" />
          </div>
        </div>
        <div className="hero-instrument" aria-label="Hydro prototype summary">
          <div className="instrument-topline">
            <span>WORK PROFILE</span>
            <strong>HYDRO / 001</strong>
          </div>
          <div className="instrument-dial" aria-hidden="true">
            <span>1.00×</span>
          </div>
          <dl>
            <div>
              <dt>Worker</dt>
              <dd>
                <RegistryId>cobblemon:squirtle</RegistryId>
              </dd>
            </div>
            <div>
              <dt>Station</dt>
              <dd>
                <RegistryId>cobblemon_kinetics:hydro_coupler</RegistryId>
              </dd>
            </div>
            <div>
              <dt>Review</dt>
              <dd>Revisioned</dd>
            </div>
          </dl>
        </div>
      </section>

      <section className="principles-section" aria-labelledby="principles-title">
        <div className="section-heading">
          <p className="eyebrow">Design principles</p>
          <h2 id="principles-title">Automation with visible intent</h2>
        </div>
        <div className="panel-grid panel-grid-three">
          <MaterialPanel eyebrow="01" title="Worker-readable">
            <p>
              Every job states who can do it, where it happens, when it stops, and how it affects
              the network.
            </p>
          </MaterialPanel>
          <MaterialPanel eyebrow="02" title="Reviewable by default">
            <p>
              Drafts stay private. Approved revisions become deterministic files before anything
              appears in a release.
            </p>
          </MaterialPanel>
          <MaterialPanel eyebrow="03" title="Universal data">
            <p>
              Namespaced identifiers and versioned contracts keep the website and Java mod
              independently buildable.
            </p>
          </MaterialPanel>
        </div>
      </section>

      <HydroLine />

      <section className="cta-strip">
        <div>
          <p className="eyebrow">Current experiment</p>
          <h2>Squirtle → Hydro Coupler</h2>
          <p>One complete path is worth more than a hundred disconnected idea cards.</p>
        </div>
        <Link className="button button-primary" href="/studio/pokemon/squirtle">
          Review the record
        </Link>
      </section>
    </main>
  );
}
