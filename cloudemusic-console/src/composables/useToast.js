import { ref } from 'vue'

const toasts = ref([])
let nextId = 0

/**
 * 全局 Toast 提示
 *
 * 在任意组件中调用：
 *   import { useToast } from '@/composables/useToast'
 *   const toast = useToast()
 *   toast.success('登录成功')
 *   toast.error('操作失败')
 *   toast.warning('请注意')
 */
export function useToast() {
  function remove(id) {
    toasts.value = toasts.value.filter((t) => t.id !== id)
  }

  function add(type, message, duration = 3000) {
    const id = nextId++
    toasts.value.push({ id, type, message })
    setTimeout(() => remove(id), duration)
  }

  return {
    toasts,
    success: (msg) => add('success', msg),
    error: (msg) => add('error', msg),
    warning: (msg) => add('warning', msg),
  }
}
