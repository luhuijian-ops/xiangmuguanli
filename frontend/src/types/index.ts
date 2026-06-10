export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

export interface User {
  id: number
  username?: string
  name: string
  email?: string
  avatar?: string
  phone?: string
  isAdmin?: boolean
  status: 'ACTIVE' | 'INACTIVE' | 'DELETED'
  createdAt: string
}

export interface Project {
  id: number
  name: string
  description?: string
  code?: string
  ownerId: number
  ownerName?: string
  status: 'ACTIVE' | 'ARCHIVED' | 'DELETED'
  priority?: 'HIGH' | 'MEDIUM' | 'LOW'
  startDate?: string
  endDate?: string
  budget?: number
  remarks?: string
  memberNames?: string[]
  fileCount?: number
  createdAt: string
  updatedAt: string
}

export interface Task {
  id: number
  title: string
  description?: string
  projectId: number
  projectCode?: string
  projectName?: string
  assigneeId?: number
  assigneeName?: string
  createdBy: number
  creatorName?: string
  status: 'TODO' | 'DOING' | 'DONE' | 'ARCHIVED'
  priority: number
  dueDate?: string
  startDate?: string
  tags?: string
  attachments?: string
  parentId?: number
  sortOrder?: number
  subtaskIds?: number[]
  dependencyIds?: number[]
  createdAt: string
  updatedAt: string
}

export interface Comment {
  id: number
  content: string
  entityType: string
  entityId: number
  userId: number
  userName?: string
  userAvatar?: string
  parentId?: number
  mentions?: string
  createdAt: string
  updatedAt: string
}

export interface File {
  id: number
  name: string
  path: string
  size: number
  type: string
  uploadedBy: number
  uploaderName?: string
  taskId?: number
  projectId?: number
  entity?: string
  createdAt: string
}

export interface Event {
  id: number
  title: string
  description?: string
  location?: string
  userId: number
  userName?: string
  projectId?: number
  projectName?: string
  taskId?: number
  startTime: string
  endTime: string
  allDay?: boolean
  reminderMinutes?: number
  repeatRule?: string
  createdAt: string
  updatedAt: string
}

export interface Milestone {
  id: number
  projectId: number
  projectName?: string
  name: string
  description?: string
  dueDate: string
  status: 'PENDING' | 'COMPLETED'
  orderIndex?: number
  createdAt: string
  updatedAt: string
}

export interface WorkHour {
  id: number
  userId: number
  userName?: string
  taskId?: number
  taskTitle?: string
  projectId?: number
  projectName?: string
  hours: number
  date: string
  description?: string
  createdAt: string
  updatedAt: string
}

export interface Alert {
  id: number
  type: 'LOGIN_FAILURE' | 'SUSPICIOUS_ACTIVITY' | 'PERMISSION_DENIED'
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
  userId?: number
  userName?: string
  targetId?: number
  targetType?: string
  message: string
  metadata?: string
  resolved: boolean
  createdAt: string
}

export interface Activity {
  id: number
  userId: number
  userName?: string
  action: string
  entityType: string
  entityId?: number
  projectId?: number
  projectName?: string
  metadata?: string
  createdAt: string
}

export interface ProjectMember {
  id: number
  projectId: number
  userId: number
  role: 'OWNER' | 'ADMIN' | 'MEMBER' | 'VIEWER'
  joinedAt: string
  user?: User
}
