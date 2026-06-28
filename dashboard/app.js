// Supabase Initialization
const SUPABASE_URL = 'https://mzgfgzojgfjeivlxtflg.supabase.co';
const SUPABASE_KEY = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im16Z2Znem9qZ2ZqZWl2bHh0ZmxnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODI2NjY5MjEsImV4cCI6MjA5ODI0MjkyMX0.0lWv-cK0orLWTuiVC5rzGcDtQADhxFthqZKPuFl1uzI';

const supabase = supabase.createClient(SUPABASE_URL, SUPABASE_KEY);

// State
let exercises = [];
let workouts = [];
let editingStepId = 0;

// DOM Elements
const tabBtns = document.querySelectorAll('.tab-btn');
const views = document.querySelectorAll('.view');
const exportBtn = document.getElementById('btn-export'); // We will change this to a Sync/Refresh button

const exercisesList = document.getElementById('exercises-list');
const workoutsList = document.getElementById('workouts-list');

const exerciseModal = document.getElementById('exercise-modal');
const workoutModal = document.getElementById('workout-modal');
const exerciseForm = document.getElementById('exercise-form');
const workoutForm = document.getElementById('workout-form');

const stepsContainer = document.getElementById('steps-container');
const btnAddStep = document.getElementById('btn-add-step');

// Setup UI
exportBtn.innerText = "Refresh Data";
exportBtn.addEventListener('click', fetchData);

// Tab Switching
tabBtns.forEach(btn => {
    btn.addEventListener('click', () => {
        tabBtns.forEach(b => b.classList.remove('active'));
        views.forEach(v => v.classList.remove('active'));
        
        btn.classList.add('active');
        document.getElementById(btn.dataset.target).classList.add('active');
    });
});

// Modals
function openModal(modalId) { document.getElementById(modalId).classList.add('active'); }
function closeModal(modalId) { document.getElementById(modalId).classList.remove('active'); }

// Data Fetching
async function fetchData() {
    if (SUPABASE_URL === 'YOUR_SUPABASE_URL_HERE') {
        alert("Please configure your Supabase URL and Key in app.js first!");
        return;
    }
    
    // Fetch Exercises
    const { data: exData, error: exErr } = await supabase.from('exercises').select('*');
    if (!exErr && exData) {
        exercises = exData;
        renderExercises();
    } else {
        console.error(exErr);
    }
    
    // Fetch Workouts and Steps
    const { data: woData, error: woErr } = await supabase.from('workouts').select('*, steps(*)');
    if (!woErr && woData) {
        workouts = woData;
        renderWorkouts();
    } else {
        console.error(woErr);
    }
}

// --- Exercises ---
document.getElementById('btn-new-exercise').addEventListener('click', () => {
    document.getElementById('exercise-modal-title').innerText = 'New Exercise';
    exerciseForm.reset();
    document.getElementById('ex-id').value = '';
    openModal('exercise-modal');
});

exerciseForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const id = document.getElementById('ex-id').value;
    const isNew = !id;
    
    const exPayload = {
        name: document.getElementById('ex-name').value,
        area: document.getElementById('ex-area').value,
        category: document.getElementById('ex-category').value,
        is_custom: document.getElementById('ex-custom').checked
    };
    
    if (!isNew) exPayload.id = id;

    const { error } = await supabase.from('exercises').upsert(exPayload);
    if (!error) {
        fetchData();
        closeModal('exercise-modal');
    } else {
        alert("Error saving exercise");
    }
});

async function deleteExercise(id) {
    if(confirm("Are you sure?")) {
        await supabase.from('exercises').delete().eq('id', id);
        fetchData();
    }
}

function renderExercises() {
    exercisesList.innerHTML = exercises.map(ex => `
        <div class="card">
            <div class="card-header">
                <h3>${ex.name}</h3>
                <span class="badge ${ex.category}">${ex.category}</span>
            </div>
            <p>Area: ${ex.area} | ${ex.is_custom ? 'Custom' : 'Standard'}</p>
            <div class="card-actions">
                <button onclick="deleteExercise('${ex.id}')" style="border-color: var(--faint-color); color: var(--faint-color)">Delete</button>
            </div>
        </div>
    `).join('');
}

// --- Workouts ---
document.getElementById('btn-new-workout').addEventListener('click', () => {
    if(exercises.length === 0) {
        alert("Please create at least one exercise first!");
        return;
    }
    document.getElementById('workout-modal-title').innerText = 'New Workout';
    workoutForm.reset();
    document.getElementById('wo-id').value = '';
    stepsContainer.innerHTML = '';
    editingStepId = 0;
    openModal('workout-modal');
});

btnAddStep.addEventListener('click', () => {
    editingStepId++;
    const stepHtml = `
        <div class="step-item" id="step-${editingStepId}">
            <div class="form-group flex-2">
                <label>Exercise</label>
                <select class="step-ex-id" required>
                    ${exercises.map(ex => `<option value="${ex.id}">${ex.name}</option>`).join('')}
                </select>
            </div>
            <div class="form-group flex-1">
                <label>Sets</label>
                <input type="number" class="step-sets" value="3" min="1" required>
            </div>
            <div class="form-group flex-1">
                <label>Work (s)</label>
                <input type="number" class="step-work" value="30" min="0" required>
            </div>
            <div class="form-group flex-1">
                <label>Rest (s)</label>
                <input type="number" class="step-rest" value="15" min="0" required>
            </div>
            <button type="button" onclick="document.getElementById('step-${editingStepId}').remove()" class="action-btn" style="border-color:var(--faint-color); color:var(--faint-color); padding:0.5rem">X</button>
        </div>
    `;
    stepsContainer.insertAdjacentHTML('beforeend', stepHtml);
});

workoutForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const woPayload = {
        name: document.getElementById('wo-name').value,
        category: document.getElementById('wo-category').value
    };

    // Insert Workout first
    const { data: woData, error: woErr } = await supabase.from('workouts').insert(woPayload).select().single();
    
    if (woErr) { alert("Error saving workout"); return; }
    const workoutId = woData.id;

    // Build & Insert Steps
    const stepEls = stepsContainer.querySelectorAll('.step-item');
    const stepsPayload = Array.from(stepEls).map((el, idx) => {
        const exId = el.querySelector('.step-ex-id').value;
        const exName = exercises.find(x => x.id === exId)?.name || 'Unknown';
        return {
            workout_id: workoutId,
            exercise_id: exId,
            exercise_name: exName,
            sets: parseInt(el.querySelector('.step-sets').value),
            work_sec: parseInt(el.querySelector('.step-work').value),
            rest_sec: parseInt(el.querySelector('.step-rest').value),
            sides: false,
            swap_sec: 5,
            sort_order: idx
        };
    });

    if (stepsPayload.length > 0) {
        await supabase.from('steps').insert(stepsPayload);
    }

    fetchData();
    closeModal('workout-modal');
});

async function deleteWorkout(id) {
    if(confirm("Are you sure?")) {
        await supabase.from('workouts').delete().eq('id', id);
        fetchData();
    }
}

function renderWorkouts() {
    workoutsList.innerHTML = workouts.map(wo => `
        <div class="card">
            <div class="card-header">
                <h3>${wo.name}</h3>
                <span class="badge ${wo.category}">${wo.category}</span>
            </div>
            <p>${wo.steps ? wo.steps.length : 0} steps</p>
            <div class="card-actions">
                <button onclick="deleteWorkout('${wo.id}')" style="border-color: var(--faint-color); color: var(--faint-color)">Delete</button>
            </div>
        </div>
    `).join('');
}

// Initial Data Load
fetchData();
