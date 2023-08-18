<template>
  <div>
    <form class="login-form" @submit.prevent="loginUser">
      <input
        class="username-input"
        type="text"
        placeholder="Username"
        v-model="userCredentials.username"
        required
      />
      <input
        class="password-input"
        type="password"
        placeholder="Password"
        v-model="userCredentials.password"
        required
      />
      <button>Login</button>
    </form>
  </div>
</template>

<script>
import authenticationService from "@/services/AuthenticationService.js";

export default {
  name: "login-user",
  data() {
    return {
      userCredentials: {
        username: "",
        password: "",
      },
    };
  },
  methods: {
    loginUser() {
      authenticationService
        .login(this.userCredentials)
        .then((response) => {
          this.$store.commit("SET_AUTHENTICATED_USER", response.data);
          this.$store.commit("SET_LOGIN_STATUS", true);
          if (this.$route.query.redirect) {
            this.$router.push(this.$route.query.redirect);
          } else {
            this.$router.push({ name: "home" });
          }
        })
        .catch((error) => {
          // Go to page saying login failed
          console.log(error);
        });
    },
  },
};
</script>

<style>
</style>