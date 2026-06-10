<template>
  <div class="schedule-calendar">
    <div class="calendar-header">
      <el-button-group>
        <el-button :icon="DArrowLeft" @click="prevYear">上一年</el-button>
        <el-button :icon="ArrowLeft" @click="prevMonth">上一月</el-button>
        <el-button @click="goToToday">今天</el-button>
        <el-button :icon="ArrowRight" @click="nextMonth">下一月</el-button>
        <el-button :icon="DArrowRight" @click="nextYear">下一年</el-button>
      </el-button-group>

      <div class="current-date">
        <h2>{{ currentYear }}年 {{ currentMonth + 1 }}月月</h2>
      </div>

      <el-button-group>
        <el-button :type="viewMode === 'month' ? 'primary' : ''" @click="viewMode = 'month'">
          月视图
        </el-button>
        <el-button :type="viewMode === 'week' ? 'primary' : ''" @click="viewMode = 'week'">
          周视图
        </el-button>
        <el-button :type="viewMode === 'day' ? 'primary' : ''" @click="viewMode = 'day'">
          日视图
        </el-button>
      </el-button-group>
    </div>

    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <div v-else class="calendar-body">
      <!-- 月视图 -->
      <div v-if="viewMode === 'month'" class="month-view">
        <div class="weekdays">
          <div v-for="day in weekDays" :key="day" class="weekday">{{ day }}</div>
        </div>
        <div class="days">
          <div
            v-for="date in monthDates"
            :key="date.key"
            :class="[
              'day-cell',
              {
                'is-other-month': !date.isCurrentMonth,
                'is-today': date.isToday,
                'is-selected': isSelectedDate(date.date),
              }
            ]"
            @click="handleDateClick(date)"
          >
            <div class="day-number">{{ date.day }}</div>
            <div class="day-events">
              <div
                v-for="event in date.events.slice(0, 3)"
                :key="event.id"
                class="event-item"
                @click.stop="handleEventClick(event)"
              >
                {{ event.title }}
              </div>
              <div v-if="date.events.length > 3" class="more-events">
                +{{ date.events.length - 3 }} 更多
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 周视图 -->
      <div v-if="viewMode === 'week'" class="week-view">
        <div class="time-slots">
          <div v-for="hour in 24" :key="hour" class="time-slot">
            {{ hour - 1 }}:00
          </div>
        </div>
        <div class="week-days">
          <div
            v-for="date in weekDates"
            :key="date.key"
            :class="['week-day', { 'is-today': date.isToday }]"
          >
            <div class="day-header">
              {{ weekDays[date.dayOfWeek] }} {{ date.day }}
            </div>
            <div class="day-events">
              <div
                v-for="event in date.events"
                :key="event.id"
                class="event-item"
                :style="getEventStyle(event)"
                @click="handleEventClick(event)"
              >
                {{ event.title }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 日视图 -->
      <div v-if="viewMode === 'day'" class="day-view">
        <div class="time-slots">
          <div v-for="hour in 24" :key="hour" class="time-slot">
            {{ hour - 1 }}:00
          </div>
        </div>
        <div class="day-events-container">
          <div
            v-for="event in dayEvents"
            :key="event.id"
            class="event-item"
            :style="getEventStyle(event)"
            @click="handleEventClick(event)"
          >
            {{ event.title }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import {
  DArrowLeft,
  ArrowLeft,
  ArrowRight,
  DArrowRight,
} from '@element-plus/icons-vue'
import { useSchedule } from '@/composables/useSchedule'
import type { Event } from '@/types'
import dayjs, { type Dayjs } from 'dayjs'

const { schedules, loading, fetchSchedules, fetchSchedulesByDateRange } = useSchedule()

const currentDate = ref(dayjs())
const viewMode = ref<'month' | 'week' | 'day'>('month')
const selectedDate = ref<Dayjs | null>(null)

const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']

const currentYear = computed(() => currentDate.value.year())
const currentMonth = computed(() => currentDate.value.month())

// 计算月视图的日期
const monthDates = computed(() => {
  const year = currentDate.value.year()
  const month = currentDate.value.month()

  const firstDayOfMonth = dayjs().year(year).month(month).date(1)
  const lastDayOfMonth = dayjs().year(year).month(month + 1).date(0)

  const firstDayOfWeek = firstDayOfMonth.day()

  const dates = []

  // 上个月的日期
  const firstDayOfMonthObj = dayjs().year(year).month(month).date(1)
  const prevMonthLastDay = firstDayOfMonthObj.subtract(1, 'day').date()
  const prevMonth = firstDayOfMonthObj.subtract(1, 'month')
  for (let i = firstDayOfWeek - 1; i >= 0; i--) {
    const day = prevMonthLastDay - i
    const date = dayjs().year(prevMonth.year()).month(prevMonth.month()).date(day)
    dates.push({
      date,
      day,
      key: date.format('YYYY-MM-DD'),
      isCurrentMonth: false,
      isToday: date.isSame(dayjs(), 'day'),
      events: getEventsForDate(date),
    })
  }

  // 当前月的日期
  for (let day = 1; day <= lastDayOfMonth.date(); day++) {
    const date = dayjs().year(year).month(month).date(day)
    dates.push({
      date,
      day,
      key: date.format('YYYY-MM-DD'),
      isCurrentMonth: true,
      isToday: date.isSame(dayjs(), 'day'),
      events: getEventsForDate(date),
    })
  }

  // 下个月的日期
  const remainingDays = 42 - dates.length
  const nextMonth = firstDayOfMonthObj.add(1, 'month')
  for (let day = 1; day <= remainingDays; day++) {
    const date = dayjs().year(nextMonth.year()).month(nextMonth.month()).date(day)
    dates.push({
      date,
      day,
      key: date.format('YYYY-MM-DD'),
      isCurrentMonth: false,
      isToday: date.isSame(dayjs(), 'day'),
      events: getEventsForDate(date),
    })
  }

  return dates
})

// 计算周视图的日期
const weekDates = computed(() => {
  const startOfWeek = currentDate.value.startOf('week')
  const dates = []

  for (let i = 0; i < 7; i++) {
    const date = startOfWeek.add(i, 'day')
    dates.push({
      date,
      day: date.date(),
      dayOfWeek: date.day(),
      key: date.format('YYYY-MM-DD'),
      isToday: date.isSame(dayjs(), 'day'),
      events: getEventsForDate(date),
    })
  }

  return dates
})

// 计算日视图的日程
const dayEvents = computed(() => {
  return getEventsForDate(currentDate.value)
})

// 获取指定日期的日程
const getEventsForDate = (date: Dayjs) => {
  return schedules.value.filter((event) => {
    const eventDate = dayjs(event.startTime)
    return eventDate.isSame(date, 'day')
  })
}

// 判断是否选中日期
const isSelectedDate = (date: Dayjs) => {
  return selectedDate.value ? date.isSame(selectedDate.value, 'day') : false
}

// 获取日程样式
const getEventStyle = (event: Event) => {
  const startHour = dayjs(event.startTime).hour()
  const endHour = dayjs(event.endTime).hour()
  const duration = endHour - startHour

  return {
    top: `${startHour * 60}px`,
    height: `${duration * 60}px`,
  }
}

const prevYear = () => {
  currentDate.value = currentDate.value.subtract(1, 'year')
  loadSchedules()
}

const prevMonth = () => {
  currentDate.value = currentDate.value.subtract(1, 'month')
  loadSchedules()
}

const nextMonth = () => {
  currentDate.value = currentDate.value.add(1, 'month')
  loadSchedules()
}

const nextYear = () => {
  currentDate.value = currentDate.value.add(1, 'year')
  loadSchedules()
}

const goToToday = () => {
  currentDate.value = dayjs()
  loadSchedules()
}

const handleDateClick = (date: any) => {
  selectedDate.value = date.date
  emit('dateClick', date.date)
}

const handleEventClick = (event: Event) => {
  emit('eventClick', event)
}

const loadSchedules = async () => {
  const startDate = currentDate.value.startOf('month').format('YYYY-MM-DD')
  const endDate = currentDate.value.endOf('month').format('YYYY-MM-DD')
  await fetchSchedulesByDateRange(startDate, endDate)
}

const emit = defineEmits<{
  dateClick: [date: Dayjs]
  eventClick: [event: Event]
}>()

onMounted(() => {
  loadSchedules()
})

// 监听日期变化，重新加载日程
watch(currentDate, loadSchedules)
</script>

<style scoped>
.schedule-calendar {
  padding: 20px;
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.current-date h2 {
  margin: 0;
  color: #333;
  font-size: 1.5em;
}

.loading-container {
  padding: 50px;
}

.calendar-body {
  min-height: 600px;
}

/* 月视图样式 */
.weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 1px;
  margin-bottom: 10px;
}

.weekday {
  text-align: center;
  font-weight: 500;
  color: #666;
  padding: 10px;
}

.days {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 1px;
}

.day-cell {
  border: 1px solid #e5e7eb;
  min-height: 100px;
  padding: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.day-cell:hover {
  background: #f5f7fa;
}

.day-cell.is-other-month {
  background: #fafafa;
  color: #999;
}

.day-cell.is-today {
  background: #e8f4ff;
  border-color: #3b82f6;
}

.day-cell.is-selected {
  background: #dbeafe;
  border-color: #3b82f6;
}

.day-number {
  font-weight: 500;
  margin-bottom: 5px;
}

.day-events {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.event-item {
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.event-meeting {
  background: #e0f2fe;
  color: #0369a1;
}

.event-reminder {
  background: #fef3c7;
  color: #b45309;
}

.event-deadline {
  background: #fee2e2;
  color: #b91c1c;
}

.more-events {
  font-size: 12px;
  color: #666;
}

/* 周视图样式 */
.week-view {
  display: flex;
  height: 600px;
}

.time-slots {
  width: 60px;
  border-right: 1px solid #e5e7eb;
}

.time-slot {
  height: 60px;
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #666;
}

.week-days {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(7, 1fr);
}

.week-day {
  border-right: 1px solid #e5e7eb;
  position: relative;
}

.week-day.is-today {
  background: #e8f4ff;
}

.day-header {
  text-align: center;
  padding: 10px;
  font-weight: 500;
  border-bottom: 1px solid #e5e7eb;
}

.day-events-container {
  position: relative;
  height: 100%;
}

/* 日视图样式 */
.day-view {
  display: flex;
  height: 600px;
}

.day-events-container {
  flex: 1;
  position: relative;
}
</style>
