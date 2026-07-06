-- Migration to update plain text passwords to BCrypt hashes

UPDATE admin_users 
SET password = '$2b$12$C7ApxUn3pOoqKXIb76xR6.2DDwbA1CVPDdxbEoS25ikyGN8T/6Ja.' 
WHERE username = 'admin' AND password = 'admin123';

UPDATE admin_users 
SET password = '$2b$12$it9dAjFiS6euGGFX29CLh.A/aLpU/g5ujc5Tarwxw8mDYLr8/LNBO' 
WHERE username = 'monish' AND password = '1234';
