INSERT INTO users (username, email, password) VALUES
('alina_dev', 'alina@example.com', '$2y$10$7SBxHP1X.Z3Zz1cI79gFGuHvVUa4eOLXzO1pJV.RyqC.I7koSPg52'),
('mark_colleague', 'mark@example.com', '$2y$10$7SBxHP1X.Z3Zz1cI79gFGuHvVUa4eOLXzO1pJV.RyqC.I7koSPg52'),
('jane_manager', 'jane@example.com', '$2y$10$7SBxHP1X.Z3Zz1cI79gFGuHvVUa4eOLXzO1pJV.RyqC.I7koSPg52');

INSERT INTO boards (title, owner_id) VALUES
('TaskFlow Development', 1),
('Personal Goals 2026', 1),
('Marketing Campaign', 2);

INSERT INTO tasks (title, description, status, priority, deadline, assignee_id, board_id, is_archived) VALUES
('Setup Database Config', 'Configure PostgreSQL and Liquibase', 'DONE', 'HIGH', '2026-01-20 10:00:00', 1, 1, false),
('Implement Spring Security', 'Add JWT and Basic Auth', 'IN_PROGRESS', 'HIGH', '2026-02-01 18:00:00', 1, 1, false),
('Design UI Mockups', 'Create Figma designs for main page', 'TODO', 'MEDIUM', '2026-02-05 12:00:00', 2, 1, false),
('Refactor Codebase', 'Clean up unused imports', 'TODO', 'LOW', NULL, NULL, 1, false),
('Old Deprecated Task', 'This should be hidden', 'DONE', 'LOW', '2025-12-31 23:59:59', 1, 1, true); -- Архівоване завдання

INSERT INTO tasks (title, description, status, priority, deadline, assignee_id, board_id, is_archived) VALUES
('Read "Clean Code"', 'Read 2 chapters per week', 'IN_PROGRESS', 'MEDIUM', '2026-03-01 10:00:00', 1, 2, false),
('Buy Gym Membership', 'Find a gym near home', 'TODO', 'LOW', NULL, 1, 2, false);

INSERT INTO comments (text, author_id, task_id, created_at) VALUES
('Hey, stuck with UserDetailsService, need help.', 1, 2, '2026-01-25 14:30:00'),
('Sure, check the repository implementation.', 2, 2, '2026-01-25 15:00:00'),
('Thanks, it works now!', 1, 2, '2026-01-25 16:45:00');