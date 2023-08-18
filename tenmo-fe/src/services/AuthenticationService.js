import axios from 'axios';
import config from '../../config/config.js';

const http = axios.create({ baseURL: config.apiBaseURL });

export default {


    register(userCredentials) {
        return http.post('/register', userCredentials);
    },

    login(userCredentials) {
        return http.post('/login', userCredentials);
    }
}
