import axios from 'axios';
import config from '../../config/config.js';

const http = axios.create({ baseURL: config.apiBaseURL });

export default {

    getAllUsers() {
        let allUsers = [];
        http.get('/users')
            .then((response) => {
                allUsers = response.data;
            }).catch((error) => {
                console.log(error);
            });
        return allUsers;
    }
}

