
import React, { useEffect, useCallback, useContext } from 'react';
import Footer from '../components/Footer';
import { useAuth } from '../providers/AuthContext';
import { useNavigate } from 'react-router-dom';
import { AlertMessageContext } from '../providers/AlertMessageContext';

const HomePage = () => {
  const navigate = useNavigate();
  const { loginUserName, isLoading, isAuthenticated, handleLogin } = useAuth();
  const { alertMessage, setAlertMessage } = useContext(AlertMessageContext);

  const handleLoginClick = () => {
    console.log('Login button clicked');
    handleLogin();
  };

  const handleAlertMessage = useCallback(async () => {
    if (alertMessage) {
      setTimeout(() => {
        setAlertMessage(null);
      }, 5000);
    }
  }, [alertMessage, setAlertMessage]);

  useEffect(() => {
    handleAlertMessage();

    function refreshPage() {
      window.location.reload();
    }
    // Set up the interval to refresh the page
    const intervalId = setInterval(refreshPage, 600000); // 600 seconds
    return () => {
      clearInterval(intervalId);
    };
  }, [handleAlertMessage]);

  if (isLoading) {
    return (
      <div className="container">
        <div className="row mt-5 ">
          <div className="col-md-12">
            <h2>Loading...</h2>
          </div>
        </div>
        <Footer />
      </div>
    );
  }

  return (
    <>
      <div className="container">
        {alertMessage && (
          <div className="alert alert-danger">{alertMessage}</div>
        )}

        <div className="row mt-5">
          {!isAuthenticated && (
            <div className="col-md-12">
              <h2>Welcome to the Note Taking App</h2>
            </div>
          )}
          {isAuthenticated && (
            <div className="col-md-12">
              <h1>Welcome Back!</h1>
              <p></p>
              <h3>{loginUserName}</h3>
            </div>
          )}
        </div>
        <div className="row mt-3">
          <div className="col-md-12">
            {!isAuthenticated && (
              <button
                type="button"
                className="btn btn-primary"
                onClick={handleLoginClick}
              >
                Login
              </button>
            )}
            {isAuthenticated && (
              <button
                type="button"
                className="btn btn-primary"
                onClick={() => navigate('/notes')}
              >
                Notes
              </button>
            )}
          </div>
        </div>

        <Footer />
      </div>
    </>
  );
};

export default HomePage;
