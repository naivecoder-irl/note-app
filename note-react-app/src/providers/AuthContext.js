/* eslint-disable react/prop-types */
import React, { createContext, useState, useEffect } from 'react';
import { useHistory, useNavigate } from 'react-router-dom';
import { generateCodeVerifier, generateCodeChallenge } from '../services/pkce';

const AuthContext = createContext();

const AuthProvider = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [accessToken, setAccessToken] = useState(null);
  const [refreshToken, setRefreshToken] = useState(null);
  const history = useHistory();
  const navigate = useNavigate();

  useEffect(() => {
    const storedAccessToken = localStorage.getItem('access_token');
    const storedRefreshToken = localStorage.getItem('refresh_token');
    if (storedAccessToken) {
      setIsAuthenticated(true);
      setAccessToken(storedAccessToken);
      if (storedRefreshToken) {
        setRefreshToken(storedRefreshToken);
      }
    }
  }, []);

  const handleLogin = async () => {
    const codeVerifier = generateCodeVerifier();
    const codeChallenge = await generateCodeChallenge(codeVerifier);

    sessionStorage.setItem('code_verifier', codeVerifier);

    //TODO replace with env variables
    const authUrl = new URL('https://your-auth-server.com/oauth/authorize');
    authUrl.searchParams.append('client_id', 'your_client_id');
    authUrl.searchParams.append('response_type', 'code');
    authUrl.searchParams.append('scope', 'your_scopes');
    authUrl.searchParams.append('redirect_uri', 'your_redirect_uri');
    authUrl.searchParams.append('code_challenge', codeChallenge);
    authUrl.searchParams.append('code_challenge_method', 'S256');

    window.location.href = authUrl.href;
  };

  const handleLogout = () => {
    setIsAuthenticated(false);
    setAccessToken(null);
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    localStorage.removeItem('id_token');
  };

  // post request to get access token, refresh token, and id token
  // TODO modified to pkce version, tokenEndpoint TODO
  const exchangeCodeForAccessToken = async (code, codeVerifier) => {
    try {
      const response = await axios.post(tokenEndpoint, {
        grant_type: 'authorization_code',
        code: code,
        redirect_uri: redirectUri,
        code_verifier: codeVerifier,
        client_id: clientId,
        client_secret: clientSecret,
      });

      const {
        access_token: accessToken,
        refresh_token: refreshToken,
        id_token: idToken,
      } = response.data;
      localStorage.setItem('access_token', accessToken);
      localStorage.setItem('refresh_token', refreshToken);
      localStorage.setItem('id_token', idToken);

      return { accessToken, refreshToken, idToken };
    } catch (error) {
      console.error('Error exchanging code for access token:', error);
      throw error;
    }
  };

  const handleAuthCallback = async (code) => {
    const codeVerifier = sessionStorage.getItem('code_verifier');
    sessionStorage.removeItem('code_verifier');

    if (codeVerifier) {
      const { accessToken, refreshToken, idToken } =
        await exchangeCodeForAccessToken(code, codeVerifier);
      //const token = await exchangeCodeForAccessToken(code, codeVerifier);
      setIsAuthenticated(true);
      setAccessToken(accessToken);
      localStorage.setItem('access_token', accessToken);
      //history.push('/');
      navigate(`/`); // TODO Redirect to the home page or another protected route
    } else {
      console.error('Missing code verifier');
    }
  };

  return (
    <AuthContext.Provider
      value={{
        isAuthenticated,
        accessToken,
        handleLogin,
        handleLogout,
        handleAuthCallback,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export { AuthContext, AuthProvider };
