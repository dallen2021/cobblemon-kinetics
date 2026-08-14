import Image from "next/image";
import { StudioPlaceholder } from "@/components/studio-placeholder";
import { StatusLamp } from "@/components/ui";

export default function AssetsPage() {
  return (
    <StudioPlaceholder
      eyebrow="Rights-first pipeline"
      title="Asset inventory"
      description="Third-party providers remain disabled until an exact source and use are approved."
      status="No external art enabled"
    >
      <div className="policy-grid">
        <div>
          <StatusLamp tone="green" label="Original interface art" />
          <p>
            Project-generated Water Worker, Hydro machinery, workshop, emblem, and planning art.
          </p>
        </div>
        <div>
          <StatusLamp tone="red" label="Third-party denied" />
          <p>No extracted Create, Minecraft, Cobblemon, Pokémon, or add-on imagery.</p>
        </div>
        <div>
          <StatusLamp tone="amber" label="Manifest required" />
          <p>
            Generation source, prompt intent, file hash, review date, and visibility are recorded.
          </p>
        </div>
      </div>
      <section className="asset-gallery" aria-labelledby="generated-art-title">
        <div className="section-heading">
          <div>
            <p className="eyebrow">Current approved set</p>
            <h2 id="generated-art-title">Generated interface art</h2>
          </div>
        </div>
        <div className="asset-gallery-grid">
          <figure className="asset-tile">
            <Image
              alt="Original Water Worker role illustration"
              height={1254}
              sizes="(max-width: 760px) calc(100vw - 4rem), (max-width: 1100px) 40vw, 220px"
              src="/art/generated/water-worker.webp"
              width={1254}
            />
            <figcaption>Water Worker · role art</figcaption>
          </figure>
          <figure className="asset-tile">
            <Image
              alt="Original Hydro Coupler workstation illustration"
              height={1254}
              sizes="(max-width: 760px) calc(100vw - 4rem), (max-width: 1100px) 40vw, 220px"
              src="/art/generated/hydro-coupler.webp"
              width={1254}
            />
            <figcaption>Hydro Coupler · workstation art</figcaption>
          </figure>
          <figure className="asset-tile">
            <Image
              alt="Abstract turbine-and-water project emblem"
              height={1254}
              sizes="(max-width: 760px) calc(100vw - 4rem), (max-width: 1100px) 40vw, 220px"
              src="/art/generated/kinetics-emblem.webp"
              width={1254}
            />
            <figcaption>Kinetics emblem · brand art</figcaption>
          </figure>
          <figure className="asset-tile">
            <Image
              alt="Workshop crate with blank blueprint and tools"
              height={1126}
              sizes="(max-width: 760px) calc(100vw - 4rem), (max-width: 1100px) 40vw, 220px"
              src="/art/generated/empty-workbench.webp"
              width={1397}
            />
            <figcaption>Workbench kit · empty-state art</figcaption>
          </figure>
        </div>
      </section>
    </StudioPlaceholder>
  );
}
