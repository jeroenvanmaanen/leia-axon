import React, { Component } from 'react';
import './App.css';
import Structure from './Structure';

class App extends Component {

  render() {
    return (
      <div className="App">
        <div className="header">LEIA learns &#187; Model Structure</div>
        <Structure />
      </div>
    );
  }
}

export default App;
