import React from 'react';
import { shallow } from 'enzyme';

import { configure } from 'enzyme';
import Adapter from 'enzyme-adapter-react-15';

configure({ adapter: new Adapter() });

import ManageAnnotationsPage from './manageAnnotationsPage';

describe('ManageAnnotationsPage', () => {
    it('renders without crashing', () => {
        shallow(
            <ManageAnnotationsPage
                annotations={[]}
                searchTerm='find me'
                canRequestMore={true}
                searchAnnotations={() => {}}
                moreAnnotations={() => {}}
                />
        );
    });
})
