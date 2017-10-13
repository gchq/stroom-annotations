import React from 'react';
import { shallow } from 'enzyme';

import { configure } from 'enzyme';
import Adapter from 'enzyme-adapter-react-15';

configure({ adapter: new Adapter() });

import NotFoundPage from './notFoundPage';

describe('NotFoundPage', () => {
    it('renders without crashing', () => {
        shallow(<NotFound />);
    });
})

