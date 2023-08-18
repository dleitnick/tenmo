<template>
  <div class="home">
  <home-page message="Welcome to Tenmo!"/>
  <account-balance v-if="$store.state.userLoggedIn"/>
  </div>
</template>

<script>
import HomePage from '@/components/HomePage.vue'
import AccountBalance from '@/components/AccountBalance.vue'

export default {
  name: 'HomeView',
  components: {
    HomePage,
    AccountBalance,
  }
}
</script>

    AccountBalance