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

window.deriveStreak = function(kind, planDays, doneDates, todayStr, max = 6) {
    const plannedDates = planDays
        .filter(day => kind === 'gym' ? day.has_gym : (kind === 'flow' ? day.has_flow : false))
        .map(day => day.date_key)
        .sort();

    const doneSet = new Set(doneDates);
    let streak = 0;

    for (const dateKey of plannedDates) {
        if (dateKey > todayStr) break;
        if (dateKey === todayStr && !doneSet.has(dateKey)) continue;
        if (doneSet.has(dateKey)) {
            streak = Math.min(max, streak + 1);
        } else {
            streak = Math.max(0, streak - 1);
        }
    }
    return streak;
};

// --- Dashboard Data Fetching ---
window.fetchDashboardData = async function() {
    if (!window.dashboardComponent) return;

    // Fetch workouts, done_log, and plan_days
    const { data: doneLog, error: logErr } = await db.from('done_log').select('*');
    const { data: workouts, error: woErr } = await db.from('workouts').select('*, steps(*)');
    const { data: planDaysData, error: planErr } = await db.from('plan_days').select('*');
    
    if (logErr) console.error(logErr);
    if (woErr) console.error(woErr);
    if (planErr) console.error(planErr);

    const planDays = planDaysData || [];
    const doneLogs = doneLog || [];

    // Calculate Streaks
    const todayStr = new Date().toISOString().split('T')[0];
    const gymDones = doneLogs.filter(d => d.category === 'gym').map(d => d.date_key);
    const flowDones = doneLogs.filter(d => d.category === 'flow').map(d => d.date_key);
    
    const gymStreak = window.deriveStreak('gym', planDays, gymDones, todayStr);
    const flowStreak = window.deriveStreak('flow', planDays, flowDones, todayStr);
    
    // Find today's plan
    const todayPlan = planDays.find(p => p.date_key === todayStr);
    let todayTitle = "Rest Day";
    let todaySubtitle = "REST · RECOVER";
    if (todayPlan) {
        if (todayPlan.has_gym && todayPlan.has_flow) {
            todayTitle = "Gym & Flow";
            todaySubtitle = "BOTH PLANNED";
        } else if (todayPlan.has_gym) {
            todayTitle = "Gym Workout";
            todaySubtitle = "GYM PLANNED";
        } else if (todayPlan.has_flow) {
            todayTitle = "Flow Session";
            todaySubtitle = "FLOW PLANNED";
        }
    }

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

    window.dashboardComponent.setState({ 
        heat, 
        sessions,
        gymStreak,
        flowStreak,
        todayTitle,
        todaySubtitle
    });
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

    let rawSteps = window.builderComponent.state.rawSteps;
    let workoutName = window.builderComponent.state.workoutName;

    const params = new URLSearchParams(window.location.search);
    const editId = params.get('edit');
    if (editId) {
        const { data: wo } = await db.from('workouts').select('*, steps(*)').eq('id', editId).single();
        if (wo) {
            workoutName = wo.name;
            window.builderComponent.state.workoutId = wo.id;
            if (wo.steps) wo.steps.sort((a,b) => a.sort_order - b.sort_order);
            rawSteps = (wo.steps || []).map(s => ({
                exercise_id: s.exercise_id,
                name: s.exercise_name,
                area: library.find(e => e.id === s.exercise_id)?.area || 'UNKNOWN',
                sets: s.sets,
                work: s.work_sec,
                rest: s.rest_sec,
                both: s.sides,
                swap: s.swap_sec
            }));
        }
    }

    window.builderComponent.setState({ library: libraryUi, rawSteps, workoutName });
};

// --- Hub Data Fetching (Gym & Flow) ---
window.fetchGymHubData = async function() {
    if (!window.gymComponent) return;
    const { data: workouts } = await db.from('workouts').select('*, steps(*)').eq('category', 'gym');
    const { data: history } = await db.from('done_log').select('*').eq('category', 'gym');
    
    const uiWorkouts = (workouts || []).map(w => ({
        id: w.id,
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
        id: w.id,
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
    const { data: planDaysData } = await db.from('plan_days').select('*');
    
    const planDays = planDaysData || [];

    // Calculate this week's dates
    const today = new Date();
    const currentDay = today.getDay(); // 0 is Sunday, 1 is Monday
    const diffToMonday = today.getDate() - currentDay + (currentDay === 0 ? -6 : 1);
    const monday = new Date(today.setDate(diffToMonday));
    
    const weekDays = [];
    const dayNames = ['MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT', 'SUN'];
    
    for (let i = 0; i < 7; i++) {
        const d = new Date(monday);
        d.setDate(monday.getDate() + i);
        const dateKey = d.toISOString().split('T')[0];
        const dayNum = String(d.getDate()).padStart(2, '0');
        const dayLabel = `${dayNames[i]} ${dayNum}`;
        
        const plan = planDays.find(p => p.date_key === dateKey);
        const hasGym = plan?.has_gym || false;
        const hasFlow = plan?.has_flow || false;
        
        let type = 'rest';
        if (hasGym && hasFlow) type = 'both';
        else if (hasGym) type = 'gym';
        else if (hasFlow) type = 'flow';

        let border, bg, accent, title, subtitle, isRest = false;

        if (type === 'gym') {
            border = '1px solid #2A2233';
            bg = 'linear-gradient(180deg,rgba(155,92,240,0.07),transparent)';
            accent = '#C7A8FF';
            title = 'Gym Workout';
            subtitle = 'GYM PLANNED';
        } else if (type === 'flow') {
            border = '1px solid #1A2733';
            bg = 'linear-gradient(180deg,rgba(79,196,240,0.07),transparent)';
            accent = '#A8EEFF';
            title = 'Flow Session';
            subtitle = 'FLOW PLANNED';
        } else if (type === 'both') {
            border = '1px solid #2A2233';
            bg = 'linear-gradient(180deg,rgba(155,92,240,0.07),transparent)';
            accent = '#C7A8FF';
            title = 'Gym & Flow';
            subtitle = 'BOTH PLANNED';
        } else {
            border = '1px dashed #262626';
            bg = 'transparent';
            accent = '#5A5A5A';
            isRest = true;
        }

        weekDays.push({
            dateKey,
            dayLabel,
            type,
            border, bg, accent, title, subtitle, isRest
        });
    }

    window.planComponent.setState({ workouts: workouts || [], weekDays });
};

window.saveWorkout = async function(workoutObj) {
    const woPayload = {
        name: workoutObj.name,
        category: workoutObj.category
    };

    let workoutId = workoutObj.id;
    if (workoutId) {
        const { error: woErr } = await db.from('workouts').update(woPayload).eq('id', workoutId);
        if (woErr) { alert("Error saving workout"); return; }
        await db.from('steps').delete().eq('workout_id', workoutId);
    } else {
        const { data: woData, error: woErr } = await db.from('workouts').insert(woPayload).select().single();
        if (woErr) { alert("Error saving workout"); return; }
        workoutId = woData.id;
    }

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

    if (e.target.closest('[data-action="delete-exercise"]')) {
        const btn = e.target.closest('[data-action="delete-exercise"]');
        const id = btn.dataset.id;
        const name = btn.dataset.name;
        if (confirm(`Are you sure you want to delete "${name}"? This will also remove it from any existing workouts.`)) {
            db.from('exercises').delete().eq('id', id).then(({error}) => {
                if (error) alert("Error: " + error.message);
                else if (window.fetchLibraryData) window.fetchLibraryData();
            });
        }
    }

    if (e.target.closest('[data-action="edit-exercise"]')) {
        const btn = e.target.closest('[data-action="edit-exercise"]');
        const id = btn.dataset.id;
        const name = prompt("Edit Exercise Name:", btn.dataset.name);
        if (!name) return;
        const area = prompt("Edit Target Area:", btn.dataset.area);
        const category = prompt("Edit Category (gym or flow):", btn.dataset.category);
        
        db.from('exercises').update({ name, area, category }).eq('id', id).then(({error}) => {
            if (error) alert("Error: " + error.message);
            else if (window.fetchLibraryData) window.fetchLibraryData();
        });
    }

    if (e.target.closest('[data-action="delete-workout"]')) {
        const btn = e.target.closest('[data-action="delete-workout"]');
        const id = btn.dataset.id;
        const name = btn.dataset.name;
        if (confirm(`Are you sure you want to delete the workout "${name}"?`)) {
            db.from('workouts').delete().eq('id', id).then(({error}) => {
                if (error) alert("Error: " + error.message);
                else {
                    if (window.fetchGymHubData) window.fetchGymHubData();
                    if (window.fetchFlowHubData) window.fetchFlowHubData();
                }
            });
        }
    }

    if (e.target.closest('[data-action="edit-workout"]')) {
        const btn = e.target.closest('[data-action="edit-workout"]');
        const id = btn.dataset.id;
        const category = btn.dataset.category;
        if (category === 'gym') {
            window.location.href = `builder.html?edit=${id}`;
        } else {
            window.location.href = `flow_builder.html?edit=${id}`;
        }
    }

    if (e.target.closest('[data-action="toggle-plan-day"]')) {
        const el = e.target.closest('[data-action="toggle-plan-day"]');
        const dateKey = el.dataset.date;
        const currentType = el.dataset.type; // 'rest', 'gym', 'flow', 'both'

        let newType = 'gym';
        if (currentType === 'rest') newType = 'gym';
        else if (currentType === 'gym') newType = 'flow';
        else if (currentType === 'flow') newType = 'both';
        else newType = 'rest';

        const payload = {
            date_key: dateKey,
            has_gym: newType === 'gym' || newType === 'both',
            has_flow: newType === 'flow' || newType === 'both',
            has_rest: newType === 'rest'
        };

        db.from('plan_days').upsert(payload).then(({error}) => {
            if (error) alert("Error: " + error.message);
            else if (window.fetchPlanData) window.fetchPlanData();
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
