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
          <p>CSS materials, machine glyphs, badges, and neutral silhouettes.</p>
        </div>
        <div>
          <StatusLamp tone="red" label="Third-party denied" />
          <p>No extracted Create, Minecraft, Cobblemon, Pokémon, or add-on imagery.</p>
        </div>
        <div>
          <StatusLamp tone="amber" label="Manifest required" />
          <p>Source, checksum, license, attribution, reviewer, and visibility are mandatory.</p>
        </div>
      </div>
    </StudioPlaceholder>
  );
}
