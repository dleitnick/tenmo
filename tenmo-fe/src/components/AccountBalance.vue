<template>
  <div>
    <table>
      <tbody>
        <tr>
          <td>Account ID:</td>
          <td>{{ account.accountId }}</td>
        </tr>
        <tr v-if="account.accountBalance">
          <td>Balance:</td>
          <td>{{ formatCurrency(account.accountBalance) }}</td>
        </tr>
        <tr>
          <td>Primary?</td>
          <td>{{ account.primary }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
import accountService from "@/services/AccountService.js";

export default {
  name: "account-balance",
  data() {
    return {
      account: {},
    };
  },
  created() {
    this.getAccount();
  },
  methods: {
    getAccount() {
      accountService
        .getPrimaryAccountBalance()
        .then((response) => {
          console.log(response.data);
          this.account = response.data;
        })
        .catch((error) => {
          console.log(error);
        });
    },
    formatCurrency(number) {
      return number.toLocaleString('en-US', { style: 'currency', currency: 'USD' });
    },
  },
};
</script>

<style>
</style>