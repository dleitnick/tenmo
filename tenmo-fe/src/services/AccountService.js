import axios from 'axios';
import config from '../../config/config.js';
import store from '../store/index.js';

const http = axios.create({ baseURL: config.apiBaseURL });

function setAuth() {
    http.defaults.headers.common['Authorization'] = `Bearer ${store.state.authenticatedUser.token}`;
}

export default {

    getPrimaryAccountBalance() {
        setAuth();
        return http.get('/balance');
    },
}
