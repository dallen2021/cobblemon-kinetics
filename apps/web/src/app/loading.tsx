export default function Loading() {
  return (
    <main className="centered-page" aria-live="polite" aria-busy="true">
      <div className="loading-machine" aria-hidden="true">
        <span />
        <span />
      </div>
      <p className="eyebrow">Bringing the line up to speed…</p>
    </main>
  );
}
