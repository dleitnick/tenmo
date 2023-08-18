<template>
  <div>
    <h1>Login</h1>
    <login-user />
  </div>
</template>

<script>
import LoginUser from "@/components/LoginUser.vue";
export default {
  name: "login-view",
  components: { LoginUser },
};
</script>

<style>
</style>