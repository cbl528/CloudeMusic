<script setup>
import { Icon } from '@iconify/vue'
import { useToast } from '@/composables/useToast'

const { toasts } = useToast()

const iconMap = {
  success: 'mdi:check-circle-outline',
  error: 'mdi:alert-circle-outline',
  warning: 'mdi:alert-outline',
}
</script>

<template>
  <Teleport to="body">
    <div class="toast-container">
      <TransitionGroup name="toast">
        <div
          v-for="t in toasts"
          :key="t.id"
          :class="['toast', t.type]"
        >
          <Icon :icon="iconMap[t.type]" width="20" height="20" class="toast-icon" />
          <span class="toast-text">{{ t.message }}</span>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<style scoped>
.toast-container {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 9999;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  pointer-events: none;
}

.toast {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 14px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  pointer-events: auto;
  min-width: 200px;
  max-width: 480px;
}

.toast.success {
  background: #f0f9eb;
  border: 1px solid #b7eb8f;
  color: #389e0d;
}
.toast.error {
  background: #fef2f2;
  border: 1px solid #fca5a5;
  color: #dc2626;
}
.toast.warning {
  background: #fffbeb;
  border: 1px solid #fcd34d;
  color: #d97706;
}

.toast-icon {
  flex-shrink: 0;
}

.toast-text {
  line-height: 1.4;
}

/* 进场/离场动画 */
.toast-enter-active {
  transition: all 0.3s ease;
}
.toast-leave-active {
  transition: all 0.25s ease;
}
.toast-enter-from {
  opacity: 0;
  transform: translateY(-16px);
}
.toast-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
