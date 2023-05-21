import axios from 'axios';
import { refreshAccessToken } from '../services/tokenService';
import * as Constants from '../constants/config';

function getLocalAccessToken() {
  const accessToken = localStorage.getItem('access_token');
  return accessToken;
}

function setLocalAccessToken(newAccessToken) {
  localStorage.setItem('access_token', newAccessToken);
}
function getLocalRefreshToken() {
  const refreshToken = localStorage.getItem('refresh_token');
  return refreshToken;
}

function setLocalRefreshToken(newRefreshToken) {
  localStorage.setItem('refresh_token', newRefreshToken);
}

function getLocalIDToken() {
  const idToken = localStorage.getItem('id_token');
  return idToken;
}

function setLocalIdToken(newIdToken) {
  localStorage.setItem('id_token', newIdToken);
}
// basic axios instance for notes api
const instance = axios.create({
  //baseURL: Constants.COURSE_API_URL,
  baseURL: process.env.REACT_APP_NOTE_URI,
  headers: { 'Content-Type': 'application/json' },
});

//TODO interceptor for access token
const requestInterceptor = instance.interceptors.request.use(
  function (config) {
    // set access token in header before request is sent
    const token = getLocalAccessToken();
    if (token) {
      // config.headers.common['Authorization'] = 'Bearer ' + token; // for Spring Boot back-end
      config.headers['Authorization'] = 'Bearer ' + token; // for Spring Boot back-end
    }
    return config;
  },
  function (error) {
    // Do something with request error
    // return Promise.reject(error);
    return Promise.reject(error.response || error.message);
  }
);

//TODO
const responseInterceptor = instance.interceptors.response.use(
  function (response) {
    // Any status code that lie within the range of 2xx cause this function to trigger
    return response;
  },
  async function (error) {
    // Any status codes that falls outside the range of 2xx cause this function to trigger
    const originalConfig = error.config;
    const refreshToken = getLocalRefreshToken();
    if (error.response) {
      if (
        refreshToken &&
        error.response.status === 401 &&
        !originalConfig._retry
      ) {
        originalConfig._retry = true;
        // call refreshToken() request
        try {
          const { newAccessToken, newRefreshToken, newIdToken } =
            await refreshAccessToken(
              //localStorage.getItem('refresh_token')
              refreshToken
            );
          setLocalAccessToken(newAccessToken);
          setLocalRefreshToken(newRefreshToken);
          setLocalIdToken(newIdToken);

          originalConfig.headers['Authorization'] = 'Bearer ' + newAccessToken;
          // return a modified instance
          return instance(originalConfig);
        } catch (error) {
          return Promise.reject(error.response.data);
          // TODO logout function to be implemented
        }
      }
    }
    return Promise.reject(error);
  }
);

export async function getSingleNote(name, id) {
  return instance({
    url: name + '/notes' + '/' + id,
    method: 'get',
  });
}

export async function getAllNotes(name) {
  return instance({
    url: name + '/notes',
    method: 'get',
  });
}
export function createNote(name, note) {
  return instance({
    url: name + '/notes',
    method: 'post',
    data: note,
  });
}

export function updateNote(name, id, note) {
  return instance({
    url: name + '/notes' + '/' + id,
    method: 'put',
    data: note,
  });
}

export function deleteNote(name, id) {
  return instance({
    url: name + '/notes' + '/' + id,
    method: 'delete',
  });
}