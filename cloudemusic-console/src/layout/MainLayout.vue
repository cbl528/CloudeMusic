<script setup>
import Sidebar from '@/components/Sidebar.vue'
import PlayerBar from '@/components/PlayerBar.vue'
import Toast from '@/components/Toast.vue'
</script>

<template>
  <Toast />
  <div class="layout">
    <Sidebar />
    <div class="right-container">
      <main class="content-area">
        <router-view />
      </main>
      <PlayerBar />
    </div>
  </div>
</template>

<style scoped>
.layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background: var(--bg-body);
}

.right-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  height: 100vh;
}

.content-area {
  flex: 1;
  overflow-y: auto;
  background: var(--bg-content);
}
</style>
