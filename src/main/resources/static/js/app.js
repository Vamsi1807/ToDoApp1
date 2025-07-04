// Replace the entire app.js with this enhanced version
document.addEventListener('DOMContentLoaded', function() {
    // DOM Elements
    const todoForm = document.getElementById('todoForm');
    const todoInput = document.getElementById('todoInput');
    const dueDateInput = document.getElementById('dueDate'); // Added for direct access
    const dueTimeInput = document.getElementById('dueTime'); // Added for direct access
    const todoList = document.getElementById('todoList');
    const getSuggestionsBtn = document.getElementById('getSuggestionsBtn');
    const suggestionsContainer = document.getElementById('suggestionsContainer');
    const suggestionsList = document.getElementById('suggestionsList');
    const chatForm = document.getElementById('chatForm');
    const chatInput = document.getElementById('chatInput');
    const chatMessages = document.getElementById('chatMessages');
    const deleteCompletedBtn = document.getElementById('deleteCompletedBtn');

    // Load todos on page load
    fetchTodos();

    // Todo Form Submission
    todoForm.addEventListener('submit', function(e) {
        e.preventDefault();
        const todoText = todoInput.value.trim();
        if (todoText) {
            const dueDate = dueDateInput.value; // Use direct reference
            const dueTime = dueTimeInput.value; // Use direct reference

            const todoItem = {
                description: todoText,
                // Combine date and time if both are provided, otherwise null
                dueDate: dueDate && dueTime ? `${dueDate}T${dueTime}:00` : null,
                completed: false
            };

            addTodoWithSuggestions(todoItem);
            todoInput.value = '';
            dueDateInput.value = ''; // Clear date input
            dueTimeInput.value = ''; // Clear time input
        }
    });

    // Get Suggestions Button
    getSuggestionsBtn.addEventListener('click', function() {
        const todoText = todoInput.value.trim();
        if (todoText) {
            getAiSuggestions(todoText);
        } else {
            alert('Please enter a todo description in the input field first.');
        }
    });

    // Chat Form Submission
    chatForm.addEventListener('submit', function(e) {
        e.preventDefault();
        const message = chatInput.value.trim();
        if (message) {
            sendChatMessage(message);
            chatInput.value = '';
        }
    });

    // Delete Completed Todos
    deleteCompletedBtn.addEventListener('click', deleteCompletedTodos);

    // Function to fetch todos
    function fetchTodos() {
        fetch('/api/todos')
            .then(response => response.json())
            .then(todos => {
                todoList.innerHTML = ''; // <-- FIX: Clears the list without re-adding form elements
                todos.forEach(todo => {
                    const todoElement = createTodoElement(todo);
                    todoList.appendChild(todoElement);
                });
            })
            .catch(error => console.error('Error fetching todos:', error));
    }

    // Function to create todo element
    function createTodoElement(todo) {
        const todoDiv = document.createElement('div');
        todoDiv.className = 'todo-item';
        if (todo.completed) {
            todoDiv.classList.add('completed');
        }

        const todoText = document.createElement('span');
        todoText.textContent = todo.description;

        const todoDetails = document.createElement('div');

        if (todo.dueDate) {
            const dueDate = new Date(todo.dueDate);
            const options = { year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' };
            const dueDateSpan = document.createElement('span');
            dueDateSpan.className = 'due-date';
            dueDateSpan.textContent = `Due: ${dueDate.toLocaleString(undefined, options)}`; // Format date nicely
            todoDetails.appendChild(dueDateSpan);
        }

        const completeBtn = document.createElement('button');
        completeBtn.className = 'complete-btn';
        completeBtn.textContent = todo.completed ? 'Undo' : 'Complete';
        completeBtn.addEventListener('click', () => toggleTodoComplete(todo.id, !todo.completed));

        const deleteBtn = document.createElement('button');
        deleteBtn.className = 'delete-btn';
        deleteBtn.textContent = 'Delete';
        deleteBtn.addEventListener('click', () => deleteTodo(todo.id)); // Event listener calls the wrapper function

        todoDetails.appendChild(completeBtn);
        todoDetails.appendChild(deleteBtn);

        todoDiv.appendChild(todoText);
        todoDiv.appendChild(todoDetails);

        return todoDiv;
    }

    // Function to add todo with suggestions
    function addTodoWithSuggestions(todoItem) {
        fetch('/api/todos/with-suggestions', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(todoItem)
        })
            .then(response => {
                if (!response.ok) {
                    // If server responds with an error, read it and throw
                    return response.text().then(text => { throw new Error('Server error: ' + text); });
                }
                return response.json();
            })
            .then(data => {
                fetchTodos(); // Refresh todo list
                if (data.suggestions) {
                    showSuggestions(data.suggestions);
                } else {
                    console.warn("No suggestions received from API.");
                    suggestionsContainer.classList.add('hidden'); // Hide if no suggestions
                }
            })
            .catch(error => console.error('Error adding todo with suggestions:', error));
    }

    // Function to delete a todo (with confirmation)
    function deleteTodo(id) {
        if (confirm('Are you sure you want to delete this todo item?')) { // CONFIRMATION ADDED
            fetch(`/api/todos/${id}`, {
                method: 'DELETE'
            })
                .then(response => {
                    if (!response.ok) {
                        return response.text().then(text => { throw new Error('Server error: ' + text); });
                    }
                    fetchTodos(); // Refresh todo list
                })
                .catch(error => console.error('Error deleting todo:', error));
        }
    }

    // Function to delete completed todos (with confirmation)
    function deleteCompletedTodos() {
        if (confirm('Are you sure you want to delete ALL completed todo items?')) { // CONFIRMATION ADDED
            fetch('/api/todos')
                .then(response => {
                    if (!response.ok) {
                        return response.text().then(text => { throw new Error('Server error: ' + text); });
                    }
                    return response.json();
                })
                .then(todos => {
                    const completedTodos = todos.filter(todo => todo.completed);
                    if (completedTodos.length === 0) {
                        alert('No completed todos to delete!');
                        return Promise.resolve(); // No-op if nothing to delete
                    }
                    const deletePromises = completedTodos.map(todo =>
                        fetch(`/api/todos/${todo.id}`, { method: 'DELETE' })
                            .then(response => {
                                if (!response.ok) {
                                    // Log individual delete errors but continue with others
                                    console.error(`Failed to delete todo ${todo.id}: ${response.status}`);
                                }
                                return response;
                            })
                    );
                    return Promise.all(deletePromises);
                })
                .then(() => {
                    fetchTodos(); // Refresh todo list after all deletions (or attempts)
                })
                .catch(error => console.error('Error deleting completed todos:', error));
        }
    }

    // Function to toggle todo completion status
    function toggleTodoComplete(id, completed) {
        fetch(`/api/todos/${id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ completed: completed })
        })
            .then(response => {
                if (!response.ok) {
                    return response.text().then(text => { throw new Error('Server error: ' + text); });
                }
                fetchTodos(); // Refresh todo list
            })
            .catch(error => console.error('Error toggling todo completion:', error));
    }

    // Function to get AI suggestions
    function getAiSuggestions(text) {
        // Send a more specific prompt for suggestions
        const promptForSuggestions = `Based on the following todo: "${text}", suggest 3-5 related, similar, or follow-up todo items. Provide only the suggestions, one per line, without numbering or bullet points.`;

        fetch('/api/chat', { // Assuming /api/chat handles generic text generation
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(promptForSuggestions) // Send a specific prompt
        })
            .then(response => {
                if (!response.ok) {
                    return response.text().then(text => { throw new Error('AI suggestion error: ' + text); });
                }
                return response.text();
            })
            .then(suggestions => {
                showSuggestions(suggestions);
            })
            .catch(error => {
                console.error('Error getting AI suggestions:', error);
                alert('Failed to get AI suggestions. Please try again later.');
                suggestionsContainer.classList.add('hidden'); // Hide suggestions on error
            });
    }

    // Function to show suggestions
    function showSuggestions(suggestions) {
        if (!suggestions || suggestions.trim() === '') {
            suggestionsContainer.classList.add('hidden');
            return;
        }

        suggestionsContainer.classList.remove('hidden');
        suggestionsList.innerHTML = '';

        // Split by new line, remove leading/trailing hyphens/bullets, filter empty lines
        const suggestionItems = suggestions.split('\n')
            .map(item => item.trim().replace(/^[*-]\s*/, '')) // Remove leading hyphens or asterisks
            .filter(item => item.length > 0);

        if (suggestionItems.length === 0) {
            suggestionsContainer.classList.add('hidden');
            return;
        }

        suggestionItems.forEach(item => {
            const li = document.createElement('li');
            li.textContent = item; // Use cleaned item
            li.addEventListener('click', () => {
                todoInput.value = item;
                suggestionsContainer.classList.add('hidden');
                suggestionsList.innerHTML = ''; // Clear suggestions after selection
            });
            suggestionsList.appendChild(li);
        });
    }

    // Function to send chat message
    function sendChatMessage(message) {
        addMessageToChat(message, 'user');

        fetch('/api/chat', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(message)
        })
            .then(response => {
                if (!response.ok) {
                    return response.text().then(text => { throw new Error('AI chat error: ' + text); });
                }
                return response.text();
            })
            .then(response => {
                addMessageToChat(response, 'ai');
            })
            .catch(error => {
                addMessageToChat("Sorry, I couldn't process your request. " + error.message, 'ai'); // Show error message from server
                console.error('Error in chat:', error);
            });
    }

    // Function to add message to chat
    function addMessageToChat(message, sender) {
        const messageDiv = document.createElement('div');
        messageDiv.classList.add(sender + '-message');
        messageDiv.textContent = message;
        chatMessages.appendChild(messageDiv);
        chatMessages.scrollTop = chatMessages.scrollHeight; // Auto-scroll to latest message
    }
});