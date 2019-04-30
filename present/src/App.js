import React, { Component } from 'react';
import './App.css';
import Structure from './Structure';

class App extends Component {

  render() {
    return (
      <div className="App">
        <div className="header">LEIA learns &#187; Model Structure</div>
        <a href='/swagger-ui.html'>
          <div class='OpenApiLink'><img src="http://localhost:3000/webjars/springfox-swagger-ui/favicon-32x32.png?v=2.9.2" alt="Open API" /> Open API</div>
        </a>
        <Structure />
      </div>
    );
  }
}

export default App;
