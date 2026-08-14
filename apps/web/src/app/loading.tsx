import Image from "next/image";

export default function Loading() {
  return (
    <main className="centered-page" aria-live="polite" aria-busy="true">
      <div className="loading-machine" aria-hidden="true">
        <Image
          className="loading-gear loading-gear-large"
          src="/art/generated/brass-gear.webp"
          alt=""
          width={48}
          height={48}
        />
        <Image
          className="loading-gear loading-gear-small"
          src="/art/generated/brass-gear.webp"
          alt=""
          width={35}
          height={35}
        />
      </div>
      <p className="eyebrow">Bringing the line up to speed…</p>
    </main>
  );
}
