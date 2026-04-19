export const stats = [
  { label: 'Repositories', value: '12' },
  { label: 'Team members', value: '8' },
  { label: 'Commits today', value: '27' },
  { label: 'Protected branches', value: '5' },
]

export const repositories = [
  {
    name: 'push-and-pray/core-engine',
    description: 'Core backend logic for repository operations, synchronization and command processing.',
    visibility: 'Private',
    language: 'Java',
    updated: 'Updated 2h ago',
    stars: 18,
  },
  {
    name: 'push-and-pray/mobile-ui',
    description: 'Responsive React interface with authentication, dashboard views and activity sections.',
    visibility: 'Public',
    language: 'React',
    updated: 'Updated today',
    stars: 31,
  },
  {
    name: 'push-and-pray/secure-auth',
    description: 'Authentication flow with role-based permissions, token sessions and access control.',
    visibility: 'Internal',
    language: 'Security',
    updated: 'Updated yesterday',
    stars: 14,
  },
  {
    name: 'push-and-pray/docs',
    description: 'System documentation, architecture notes and workflow references.',
    visibility: 'Public',
    language: 'Markdown',
    updated: 'Updated 3 days ago',
    stars: 9,
  },
]

export const activity = [
  { icon: '📝', title: 'Commit pushed to core-engine', text: 'Authentication workflow updated · 5 min ago' },
  { icon: '👥', title: 'New collaborator added', text: 'Repository permissions updated · 18 min ago' },
  { icon: '🛡️', title: 'Security policy changed', text: 'Protected access rules modified · 42 min ago' },
  { icon: '✅', title: 'Status check completed', text: 'Workspace synchronized successfully · 1 hour ago' },
]

export const teamMembers = [
  { initials: 'AP', name: 'Ana Petrova', role: 'Administrator', description: 'Full access to repositories and team settings.' },
  { initials: 'GK', name: 'Georgi Kolev', role: 'Maintainer', description: 'Repository operations, fetch and merge workflows.' },
  { initials: 'MM', name: 'Mariya Marinova', role: 'Contributor', description: 'Commit and branch activity permissions.' },
  { initials: 'IV', name: 'Ivan Velikov', role: 'Contributor', description: 'UI changes and documentation workflows.' },
]

export const profile = {
  name: 'Ana Petrova',
  email: 'ana.petrova@pushandpray.dev',
  role: 'Administrator',
  ownedRepositories: 4,
  collaborativeProjects: 8,
  pendingApprovals: 2,
}
//
// // 1. Първо импортни функцията за токена най-горе във файла
// import { getAccessToken } from '../components/auth';
//
// // 2. Намери функцията fetchRepositories и я направи така:
// const fetchRepositories = async () => {
//   try {
//     const token = getAccessToken(); // Вземаме "ключа" от Keycloak
//
//     const response = await fetch('http://localhost:8080/api/repositories', {
//       method: 'GET',
//       headers: {
//         'Content-Type': 'application/json',
//         // ЕТО ТОВА Е ТОЧКА 3 - изпращаме токена към Spring Boot
//         'Authorization': `Bearer ${token}`
//       }
//     });
//
//     if (!response.ok) {
//       throw new Error('Грешка при зареждане на данните');
//     }
//
//     const data = await response.json();
//     setRepositories(data); // Записваме данните в екрана
//   } catch (error) {
//     console.error("Грешка:", error);
//   }
// };
