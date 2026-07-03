// The signature element: a goal plotted as a trajectory rising to a target.
// One bold moment; everything else in the app stays quiet.

export default function TrajectoryPanel() {
  return (
    <div className="relative flex h-full flex-col justify-between overflow-hidden bg-navy p-8 lg:p-12">
      {/* ambient glow */}
      <div
        className="pointer-events-none absolute -right-24 -top-24 h-72 w-72 rounded-full opacity-40 blur-3xl"
        style={{ background: 'radial-gradient(circle, #1D3160 0%, transparent 70%)' }}
      />

      <div className="relative flex items-center gap-2.5">
        <TargetMark />
        <span className="font-display text-lg font-bold tracking-tight text-white">Goalkeeper</span>
      </div>

      {/* plot */}
      <svg
        viewBox="0 0 440 420"
        className="relative my-6 hidden w-full lg:block"
        role="img"
        aria-label="A trajectory rising toward a target"
      >
        <defs>
          <linearGradient id="arc" x1="0" y1="1" x2="1" y2="0">
            <stop offset="0%" stopColor="#2647CE" />
            <stop offset="100%" stopColor="#E8A23B" />
          </linearGradient>
        </defs>

        {/* faint grid */}
        {[80, 160, 240, 320].map((y) => (
          <line key={`h${y}`} x1="20" y1={y} x2="420" y2={y} stroke="#1D3160" strokeWidth="1" />
        ))}
        {[110, 210, 310].map((x) => (
          <line key={`v${x}`} x1={x} y1="30" x2={x} y2="380" stroke="#16264A" strokeWidth="1" />
        ))}

        {/* baseline */}
        <line x1="20" y1="380" x2="420" y2="380" stroke="#2A3E68" strokeWidth="1.5" />

        {/* the trajectory */}
        <path
          className="traj-path"
          d="M40 372 C 170 372, 300 300, 398 70"
          fill="none"
          stroke="url(#arc)"
          strokeWidth="3.5"
          strokeLinecap="round"
        />

        {/* current position marker */}
        <g className="traj-dot">
          <circle cx="222" cy="322" r="6" fill="#EDF0F5" />
          <circle cx="222" cy="322" r="11" fill="none" stroke="#EDF0F5" strokeOpacity="0.4" strokeWidth="1.5" />
          <text x="238" y="318" className="font-mono" fontSize="11" fill="#8FA0C4">you are here</text>
        </g>

        {/* target */}
        <g className="traj-target">
          <circle cx="398" cy="70" r="16" fill="none" stroke="#E8A23B" strokeOpacity="0.35" strokeWidth="1.5" />
          <circle cx="398" cy="70" r="7" fill="#E8A23B" />
        </g>
      </svg>

      <div className="relative">
        <p className="font-display text-2xl font-bold leading-tight text-white lg:text-3xl">
          Aim set.<br />Course plotted.
        </p>
        <p className="mt-3 max-w-xs text-sm leading-relaxed text-white/60">
          Turn a distant intention into a dated target with checkpoints along the way — and
          keep sight of it every day.
        </p>
      </div>
    </div>
  );
}

function TargetMark() {
  return (
    <svg width="22" height="22" viewBox="0 0 22 22" aria-hidden="true">
      <circle cx="11" cy="11" r="9" fill="none" stroke="#E8A23B" strokeWidth="1.6" />
      <circle cx="11" cy="11" r="4" fill="none" stroke="#E8A23B" strokeWidth="1.6" />
      <circle cx="11" cy="11" r="1.4" fill="#E8A23B" />
    </svg>
  );
}
