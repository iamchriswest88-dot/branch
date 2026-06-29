const SUPABASE_URL = 'https://mzgfgzojgfjeivlxtflg.supabase.co';
const SUPABASE_KEY = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im16Z2Znem9qZ2ZqZWl2bHh0ZmxnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODI2NjY5MjEsImV4cCI6MjA5ODI0MjkyMX0.0lWv-cK0orLWTuiVC5rzGcDtQADhxFthqZKPuFl1uzI';

window.addEventListener('error', function(e) {
    const errDiv = document.createElement('div');
    errDiv.style.cssText = 'position:fixed;top:0;left:0;width:100%;background:red;color:white;z-index:9999;padding:10px;font-family:monospace;';
    errDiv.innerText = 'JS Error: ' + e.message + ' at ' + e.filename + ':' + e.lineno;
    document.body.appendChild(errDiv);
});

window.addEventListener('unhandledrejection', function(e) {
    const errDiv = document.createElement('div');
    errDiv.style.cssText = 'position:fixed;top:0;left:0;width:100%;background:red;color:white;z-index:9999;padding:10px;font-family:monospace;';
    errDiv.innerText = 'Promise Error: ' + (e.reason ? e.reason.message || e.reason : 'Unknown');
    document.body.appendChild(errDiv);
});

const db = supabase.createClient(SUPABASE_URL, SUPABASE_KEY);

const GYM = '#9B5CF0', FLOW = '#4FC4F0', REST = '#181818';

// --- Dashboard Data Fetching ---
window.fetchDashboardData = async function() {
    if (!window.dashboardComponent) return;

    // Fetch workouts and done_log
    const { data: doneLog, error: logErr } = await db.from('done_log').select('*');
    const { data: workouts, error: woErr } = await db.from('workouts').select('*, steps(*)');
    
    if (logErr) console.error(logErr);
    if (woErr) console.error(woErr);

    // Build Heatmap (30 weeks * 7 days)
    const heat = [];
    const today = new Date();
    // Go back 210 days
    for (let i = 210; i >= 1; i--) {
        const d = new Date(today);
        d.setDate(d.getDate() - i + 1);
        const dateKey = d.toISOString().split('T')[0];
        const logs = doneLog ? doneLog.filter(l => l.date_key === dateKey) : [];
        
        let color = REST;
        if (logs.length > 0) {
            const hasGym = logs.some(l => l.category === 'gym');
            const hasFlow = logs.some(l => l.category === 'flow');
            const intensity = Math.min(logs.length, 3);
            const a = intensity >= 3 ? 1 : intensity === 2 ? 0.6 : 0.32;
            const base = hasGym ? '155, 92, 240' : '79, 196, 240';
            color = `rgba(${base}, ${a})`;
        }
        heat.push({ color });
    }

    // Build Recent Sessions (mocked from done_log for now)
    const recentLogs = doneLog ? [...doneLog].reverse().slice(0, 6) : [];
    const sessions = recentLogs.map((l, i) => {
        const type = l.category.toUpperCase();
        return {
            date: l.date_key,
            type: type,
            workout: `Session ${recentLogs.length - i}`, // Default name since done_log lacks workout link
            duration: '20:00',
            volume: '10 sets',
            delta: '+1',
            color: l.category === 'gym' ? GYM : FLOW,
            deltaColor: '#86D17A'
        };
    });

    // We can also compute totals from workouts
    window.dashboardComponent.setState({ heat, sessions });
};

// --- Builder Data Fetching & Logic ---
window.fetchBuilderData = async function() {
    if (!window.builderComponent) return;

    const { data: exercises, error: exErr } = await db.from('exercises').select('*');
    if (exErr) console.error(exErr);

    const library = exercises || [];
    
    // Convert library for UI
    const libraryUi = library.map(ex => ({
        id: ex.id,
        name: ex.name,
        area: ex.area,
        category: ex.category
    }));

    window.builderComponent.setState({ library: libraryUi });
};

// --- Hub Data Fetching (Gym & Flow) ---
window.fetchGymHubData = async function() {
    if (!window.gymComponent) return;
    const { data: workouts } = await db.from('workouts').select('*, steps(*)').eq('category', 'gym');
    const { data: history } = await db.from('done_log').select('*').eq('category', 'gym');
    
    const uiWorkouts = (workouts || []).map(w => ({
        name: w.name,
        stepCount: w.steps ? w.steps.length : 0
    }));
    
    const uiHistory = (history || []).map(h => ({
        date: h.date_key,
        workout: 'Gym Session', // the schema doesn't link done_log to workout names
        duration: '45 MIN' // placeholder
    }));
    
    window.gymComponent.setState({ workouts: uiWorkouts, history: uiHistory });
};

window.fetchFlowHubData = async function() {
    if (!window.flowComponent) return;
    const { data: workouts } = await db.from('workouts').select('*, steps(*)').eq('category', 'flow');
    const { data: history } = await db.from('done_log').select('*').eq('category', 'flow');
    
    const uiWorkouts = (workouts || []).map(w => ({
        name: w.name,
        stepCount: w.steps ? w.steps.length : 0
    }));
    
    const uiHistory = (history || []).map(h => ({
        date: h.date_key,
        workout: 'Flow Session',
        duration: '20 MIN'
    }));
    
    window.flowComponent.setState({ workouts: uiWorkouts, history: uiHistory });
};

// --- Builder Data Fetching & Logic ---

// --- Library & Plan ---
window.fetchLibraryData = async function() {
    if (!window.libraryComponent) return;
    const { data: exercises } = await db.from('exercises').select('*');
    window.libraryComponent.setState({ exercises: exercises || [] });
};

window.fetchPlanData = async function() {
    if (!window.planComponent) return;
    const { data: workouts } = await db.from('workouts').select('*, steps(*)');
    window.planComponent.setState({ workouts: workouts || [] });
};

window.saveWorkout = async function(workoutObj) {
    // 1. Insert Workout
    const woPayload = {
        name: workoutObj.name,
        category: workoutObj.category
    };

    const { data: woData, error: woErr } = await db.from('workouts').insert(woPayload).select().single();
    if (woErr) { alert("Error saving workout"); return; }
    const workoutId = woData.id;

    // 2. Insert Steps
    const stepsPayload = workoutObj.rawSteps.map((s, idx) => ({
        workout_id: workoutId,
        exercise_id: s.exercise_id,
        exercise_name: s.name,
        sets: s.sets,
        work_sec: s.work,
        rest_sec: s.rest,
        sides: s.both,
        swap_sec: s.swap,
        sort_order: idx
    }));

    if (stepsPayload.length > 0) {
        await db.from('steps').insert(stepsPayload);
    }
    
    alert("Workout Saved!");
    window.location.href = "index.html"; // Go back to dashboard
};

// --- Global Event Delegation ---
document.addEventListener('click', (e) => {
    // Navigation
    if (e.target.closest('[data-action="nav-dashboard"]')) window.location.href = 'index.html';
    if (e.target.closest('[data-action="nav-gym"]')) window.location.href = 'gym.html';
    if (e.target.closest('[data-action="nav-flow"]')) window.location.href = 'flow.html';
    if (e.target.closest('[data-action="nav-plan"]')) window.location.href = 'plan.html';
    if (e.target.closest('[data-action="nav-library"]')) window.location.href = 'library.html';

    // Hub Actions
    if (e.target.closest('[data-action="new-gym-workout"]')) window.location.href = 'builder.html';
    if (e.target.closest('[data-action="new-flow-workout"]')) window.location.href = 'flow_builder.html';

    // Library Actions
    if (e.target.closest('[data-action="toggle-lib-gym"]')) {
        if (window.libraryComponent) window.libraryComponent.toggleAcc('gym');
    }
    if (e.target.closest('[data-action="toggle-lib-flow"]')) {
        if (window.libraryComponent) window.libraryComponent.toggleAcc('flow');
    }
    
    if (e.target.closest('[data-action="add-exercise-prompt"]')) {
        const name = prompt("Enter Exercise Name:");
        if (!name) return;
        const area = prompt("Enter Target Area (e.g., Chest, Quads):", "Full Body");
        const category = prompt("Enter Category (gym or flow):", "gym");
        
        db.from('exercises').insert([{ name, area, category, is_custom: true }])
          .then(({ error }) => {
              if (error) alert("Error: " + error.message);
              else if (window.fetchLibraryData) window.fetchLibraryData();
          });
    }

    // Builder Actions
    const addBtn = e.target.closest('[data-action="add-step"]');
    if (addBtn && window.builderComponent) {
        window.builderComponent.addStep(addBtn.dataset.id);
    }

    const removeBtn = e.target.closest('[data-action="remove-step"]');
    if (removeBtn && window.builderComponent) {
        window.builderComponent.removeStep(parseInt(removeBtn.dataset.idx, 10));
    }

    const counterBtn = e.target.closest('[data-action="update-counter"]');
    if (counterBtn && window.builderComponent) {
        window.builderComponent.updateCounter(
            parseInt(counterBtn.dataset.idx, 10),
            counterBtn.dataset.field,
            parseInt(counterBtn.dataset.delta, 10)
        );
    }

    const bothBtn = e.target.closest('[data-action="toggle-both"]');
    if (bothBtn && window.builderComponent) {
        window.builderComponent.toggleBothSides(parseInt(bothBtn.dataset.idx, 10));
    }

    const saveBtn = e.target.closest('[data-action="save-workout"]');
    if (saveBtn && window.builderComponent) {
        window.builderComponent.save();
    }
});
