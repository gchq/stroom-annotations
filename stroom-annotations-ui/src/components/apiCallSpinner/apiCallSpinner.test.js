import React from 'react';
import { shallow } from 'enzyme';

import { configure } from 'enzyme';
import Adapter from 'enzyme-adapter-react-15';

configure({ adapter: new Adapter() });

import ApiCallSpinner from './apiCallSpinner';

describe('ApiCallSpinner', () => {
    it('renders without crashing', () => {
        shallow(
            <ApiCallSpinner
                isSpinning={true}
                />
        );
    });
})