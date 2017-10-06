import React from 'react';
import { shallow } from 'enzyme';

import { configure } from 'enzyme';
import Adapter from 'enzyme-adapter-react-15';

configure({ adapter: new Adapter() });

import ManageAnnotations from './manageAnnotations';

describe('ManageAnnotations', () => {
    it('renders without crashing', () => {
        shallow(
            <ManageAnnotations
                annotations={[]}
                searchTerm='find me'
                canRequestMore={true}
                searchAnnotations={() => {}}
                moreAnnotations={() => {}}
                />
        );
    });
})
