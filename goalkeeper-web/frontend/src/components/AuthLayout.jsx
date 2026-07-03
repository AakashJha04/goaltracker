import TrajectoryPanel from './TrajectoryPanel';

export default function AuthLayout({ children }) {
  return (
    <div className="min-h-screen bg-canvas lg:grid lg:grid-cols-[minmax(0,1fr)_minmax(0,1.1fr)]">
      {/* Signature panel: full height on desktop, slim banner on mobile. */}
      <div className="h-36 lg:h-auto">
        <TrajectoryPanel />
      </div>

      {/* Form column */}
      <div className="flex items-center justify-center px-6 py-12 lg:px-16">
        <div className="w-full max-w-sm">{children}</div>
      </div>
    </div>
  );
}
