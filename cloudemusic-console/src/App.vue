<script setup>
import { onMounted } from 'vue'
import { RouterView } from 'vue-router'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

onMounted(() => {
  // 页面刷新后从 localStorage 恢复登录状态
  userStore.init()
})
</script>

<template>
  <RouterView />
</template>
