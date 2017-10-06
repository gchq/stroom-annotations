import React from 'react';
import ReactDOM from 'react-dom';

import SingleAnnotation from './singleAnnotation';


describe('SingleAnnotation', () => {
    it('renders without crashing', () => {
        const div = document.createElement('div');
        ReactDOM.render(<SingleAnnotation />, div);
    });
})

