import { ref } from 'vue'

const showLoginModal = ref(false)

export function useAuthModal() {
  function requireLogin() {
    showLoginModal.value = true
    return false
  }

  return { showLoginModal, requireLogin }
}
