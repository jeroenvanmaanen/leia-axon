import React, { Component } from 'react';
import './App.css';

class App extends Component {

  render() {
    console.log('UUID', UUID);
    const uuid = UUID.create();
    console.log('uuid', uuid);
    return (
      <div className="App">
        <!-- TODO: Add greeter -->
      </div>
    );
  }
}

export default App;
