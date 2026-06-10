import dayjs from 'dayjs'

/**
 * 格式化日期
 * @param date 日期字符串或Date对象
 * @param format 格式化字符串，默认 'YYYY-MM-DD HH:mm:ss'
 * @returns 格式化后的日期字符串
 */
export function formatDate(date: string | Date | undefined, format: string = 'YYYY-MM-DD HH:mm:ss'): string {
  if (!date) return ''
  return dayjs(date).format(format)
}

/**
 * 格式化日期（简短格式）
 * @param date 日期字符串或Date对象
 * @returns 格式化后的日期字符串
 */
export function formatDateShort(date: string | Date | undefined): string {
  return formatDate(date, 'YYYY-MM-DD')
}

/**
 * 格式化日期时间（简短格式）
 * @param date 日期字符串或Date对象
 * @returns 格式化后的日期字符串
 */
export function formatDateTime(date: string | Date | undefined): string {
  return formatDate(date, 'YYYY-MM-DD HH:mm')
}

/**
 * 获取相对时间
 * @param date 日期字符串或Date对象
 * @returns 相对时间字符串
 */
export function getRelativeTime(date: string | Date | undefined): string {
  if (!date) return ''
  const now = dayjs()
  const target = dayjs(date)
  const diffMinutes = now.diff(target, 'minute')

  if (diffMinutes < 1) return '刚刚'
  if (diffMinutes < 60) return `${diffMinutes}分钟前`

  const diffHours = now.diff(target, 'hour')
  if (diffHours < 24) return `${diffHours}小时前`

  const diffDays = now.diff(target, 'day')
  if (diffDays < 7) return `${diffDays}天前`

  return formatDateShort(date)
}

/**
 * 判断是否是今天
 * @param date 日期字符串或Date对象
 * @returns 是否是今天
 */
export function isToday(date: string | Date | undefined): boolean {
  if (!date) return false
  const target = dayjs(date)
  const now = dayjs()
  return target.isSame(now, 'day')
}

/**
 * 判断是否是过去
 * @param date 日期字符串或Date对象
 * @returns 是否是过去
 */
export function isPast(date: string | Date | undefined): boolean {
  if (!date) return false
  return dayjs(date).isBefore(dayjs())
}

/**
 * 判断是否是未来
 * @param date 日期字符串或Date对象
 * @returns 是否是未来
 */
export function isFuture(date: string | Date | undefined): boolean {
  if (!date) return false
  return dayjs(date).isAfter(dayjs())
}

export default {
  formatDate,
  formatDateShort,
  formatDateTime,
  getRelativeTime,
  isToday,
  isPast,
  isFuture
}
