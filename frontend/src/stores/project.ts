import { defineStore } from 'pinia'
import { projectApi } from '@/api'
import type { Project } from '@/types'

interface ProjectState {
  myProjects: Project[]
  currentProject: Project | null
  projectList: Project[]
}

export const useProjectStore = defineStore('project', {
  state: (): ProjectState => ({
    myProjects: [],
    currentProject: null,
    projectList: [],
  }),

  getters: {
    getProjectById: (state) => (id: number | string) => {
      return state.myProjects.find(p => p.id.toString() === id.toString())
    },
    isProjectMember: (state) => (projectId: number | string) => {
      return state.myProjects.some(p => p.id.toString() === projectId.toString())
    },
    isProjectOwnerOrAdmin: (state) => (projectId: number | string) => {
      const project = state.myProjects.find(p => p.id.toString() === projectId.toString())
      if (!project) return false
      const role = (project as any).role
      return role === 'OWNER' || role === 'ADMIN'
    },
  },

  actions: {
    setProjectList(projects: Project[]) {
      this.projectList = projects
      this.myProjects = projects
    },

    async fetchMyProjects() {
      try {
        const response = await projectApi.getMyProjects({ page: 0, pageSize: 1000 })
        if (response && response.data && response.data.code === 200) {
          const data = response.data.data
          const projects = data.content || data || []
          this.myProjects = projects
          this.projectList = projects
        }
      } catch (error) {
        // request interceptor already handles error messages
      }
    },

    async addProjectToMyList(project: Project) {
      if (!this.myProjects.some(p => p.id === project.id)) {
        this.myProjects.unshift(project)
        this.projectList.unshift(project)
      }
    },

    setCurrentProject(project: Project | null) {
      this.currentProject = project
    },

    clearProjects() {
      this.myProjects = []
      this.currentProject = null
      this.projectList = []
    },
  },
})
