import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    apiBaseURL: "http://localhost:8080",
    userLoggedIn: false,
    user: {
      id: -1,
      username: "",
    },
    authenticatedUser: {
      token: "",
      user: {},
    },
    userCredentials: {
      username: "",
      password: "",
    },
    account: {
      id: -1,
      userId: -1,
      accountBalance: 0,
      isPrimary: false,
    }
  },
  getters: {
  },
  mutations: {
    SET_AUTHENTICATED_USER(state, authenticatedUser) {
      state.authenticatedUser = authenticatedUser;
    },
    SET_LOGIN_STATUS(state, status) {
      state.userLoggedIn = status;
    },
  },
  actions: {
  },
  modules: {
  }
});
